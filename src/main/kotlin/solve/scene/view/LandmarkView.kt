package solve.scene.view

import javafx.beans.InvalidationListener
import javafx.beans.WeakInvalidationListener
import javafx.collections.SetChangeListener
import javafx.collections.WeakSetChangeListener
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Shape
import javafx.util.Duration
import solve.scene.model.Landmark
import solve.scene.view.drawing.FrameDrawer

// Responsive for creating and setting visual effects for landmarks presenting controls
// This has access to landmark data class and its layer
sealed class LandmarkView(
    scale: Double,
    viewOrder: Int,
    private val landmark: Landmark,
) {
    companion object {
        fun create(landmark: Landmark, viewOrder: Int, scale: Double, frameDrawer: FrameDrawer, canvasNode: Node): LandmarkView {
            return when (landmark) {
                is Landmark.Keypoint -> KeypointView(landmark, viewOrder, scale)
                is Landmark.Line -> LineView(landmark, viewOrder, scale)
                is Landmark.Plane -> PlaneView(landmark, frameDrawer, canvasNode, viewOrder, scale)
            }
        }

        val HighlightingAnimationDuration: Duration = Duration.millis(500.0)
        val InstantAnimationDuration: Duration = Duration.millis(0.1)
    }

    // When shape is created in an inheritor
    // setUpShape() should be called to set up common features for all landmarks
    abstract val node: Node?

    // Ensures right z-order between landmarks from different layers
    // Landmark with greater viewOrder will be drawn above
    // Node.viewOrder is double, and node with less viewOrder will be drawn above
    var viewOrder: Int = viewOrder.also { node?.viewOrder = LANDMARKS_VIEW_ORDER - viewOrder }
        set(value) {
            node?.viewOrder = LANDMARKS_VIEW_ORDER - viewOrder
            field = value
            viewOrderChanged()
        }

    abstract fun addToFrameDrawer()

    private val layerState = landmark.layerState
    private val layerSettings = landmark.layerSettings

    protected val isSelected get() = layerState.selectedLandmarksUids.contains(landmark.uid)
    protected val isHovered get() = layerState.hoveredLandmarksUids.contains(landmark.uid)
    protected val shouldHighlight get() = isSelected || isHovered

    private var isHighlighted = false

    var scale: Double = scale
        set(value) {
            field = value
            scaleChanged()
        }

    private val parentChangedListener: InvalidationListener = InvalidationListener { newValue ->
        if (newValue != null && shouldHighlight) {
            highlightShapeIfNeeded(InstantAnimationDuration)
        }
        removeParentChangedListener()
    }
    private val weakParentChangedListener = WeakInvalidationListener(parentChangedListener)

    private fun removeParentChangedListener() = node?.parentProperty()?.removeListener(weakParentChangedListener)

    // Should be stored to avoid weak listener from be collected
    private val selectedLandmarksChangedEventHandler = SetChangeListener<Long> { e ->
        if (e.wasAdded() && e.elementAdded == landmark.uid) {
            highlightShapeIfNeeded(HighlightingAnimationDuration)
        }

        if (e.wasRemoved() && e.elementRemoved == landmark.uid) {
            unhighlightShapeIfNeeded(HighlightingAnimationDuration)
        }
    }
    private val weakSelectedLandmarksChangedEventHandler = WeakSetChangeListener(selectedLandmarksChangedEventHandler)

    // Should be stored to avoid weak listener from be collected
    private val hoveredLandmarksChangedEventHandler = SetChangeListener<Long> { e ->
        if (isSelected) {
            return@SetChangeListener
        }

        if (e.wasAdded() && e.elementAdded == landmark.uid) {
            highlightShapeIfNeeded(HighlightingAnimationDuration)
        }

        if (e.wasRemoved() && e.elementRemoved == landmark.uid) {
            unhighlightShapeIfNeeded(HighlightingAnimationDuration)
        }
    }
    private val weakHoveredLandmarksChangedEventHandler = WeakSetChangeListener(hoveredLandmarksChangedEventHandler)

    private val useOneColorChangedListener = InvalidationListener { useOneColorChanged() }
    private val weakUseOneColorChangedListener = WeakInvalidationListener(useOneColorChangedListener)

    init {
        landmark.layerState.selectedLandmarksUids.addListener(weakSelectedLandmarksChangedEventHandler)
        landmark.layerState.hoveredLandmarksUids.addListener(weakHoveredLandmarksChangedEventHandler)

        landmark.layerSettings.useOneColor.addListener(weakUseOneColorChangedListener)
    }

    open fun dispose() {
        layerState.selectedLandmarksUids.removeListener(weakSelectedLandmarksChangedEventHandler)
        layerState.hoveredLandmarksUids.removeListener(weakHoveredLandmarksChangedEventHandler)

        layerSettings.useOneColor.removeListener(weakUseOneColorChangedListener)
    }

    // Set up common shape properties
    // Can not be called during LandmarkView initialization because shape is created by inheritors
    protected fun setUpShape(shape: Shape, uid: Long) {
        shape.parentProperty().addListener(weakParentChangedListener)

        shape.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (isSelected) {
                layerState.selectedLandmarksUids.remove(uid)
                return@addEventHandler
            }
            layerState.selectedLandmarksUids.add(uid)
        }

        shape.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            layerState.hoveredLandmarksUids.add(uid)
        }

        shape.addEventHandler(MouseEvent.MOUSE_EXITED) {
            layerState.hoveredLandmarksUids.remove(uid)
        }
    }

    protected fun toFront(node: Node) {
        node.viewOrder -= HIGHLIGHTING_VIEW_ORDER_GAP
    }

    protected fun toBack(node: Node) {
        node.viewOrder += HIGHLIGHTING_VIEW_ORDER_GAP
    }

    protected abstract fun scaleChanged()

    protected abstract fun useOneColorChanged()

    protected abstract fun viewOrderChanged()

    // Should be called only from highlightShapeIfNeeded
    protected abstract fun highlightShape(duration: Duration)

    // Should be called only from unhighlightShapeIfNeeded
    protected abstract fun unhighlightShape(duration: Duration)

    protected fun highlightShapeIfNeeded(duration: Duration) {
        if (isHighlighted) {
            return
        }
        highlightShape(duration)
        isHighlighted = true
    }

    private fun unhighlightShapeIfNeeded(duration: Duration) {
        if (!isHighlighted) {
            return
        }
        unhighlightShape(duration)
        isHighlighted = false
    }
}
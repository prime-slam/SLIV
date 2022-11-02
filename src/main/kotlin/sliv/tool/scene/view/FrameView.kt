package sliv.tool.scene.view

import io.github.palexdev.virtualizedfx.cell.GridCell
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.*
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import sliv.tool.scene.model.*
import tornadofx.*

class FrameView(width: Double, height: Double, frame: VisualizationFrame?) : Group(), GridCell<VisualizationFrame?> {
    private var frame: VisualizationFrame? = null
    private var landmarksViews: Map<Layer, List<LandmarkView>>? = null
    private var fakeImageColor: Color? = null
    private val canvas = Canvas(width, height)

    init {
        updateItem(frame)
        canvas.onMouseMoved = EventHandler { event: MouseEvent ->
            onMouseMoved(event)
        }
        add(canvas)
    }

    override fun getNode(): Node {
        return this
    }

    override fun updateItem(frame: VisualizationFrame?) {
        this.frame = frame
        landmarksViews = frame?.landmarks?.mapValues { it.value.map { landmark -> LandmarkView.create(landmark) } }
        fakeImageColor = generateRandomColor()
        draw()
    }

    private fun draw() {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
//        gc.drawImage(frame.image, 0.0, 0.0)
        drawFakeImage() //TODO: real images can be used only with data virtualization
        doForAllEnabledLandmarks { view -> view.draw(gc) }
    }

    private fun drawFakeImage() {
        val gc = canvas.graphicsContext2D
        gc.fill = fakeImageColor
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
    }

    private fun doForAllEnabledLandmarks(delegate: (LandmarkView) -> Unit) {
        landmarksViews?.forEach { (layer, landmarkViews) ->
            if (!layer.enabled) {
                return
            }
            landmarkViews.forEach { view -> delegate(view) }
        }
    }

    private fun onMouseMoved(event: MouseEvent) {
        var stateChanged = false
        doForAllEnabledLandmarks { view ->
            val prevState = view.state
            view.updateIsHovered(event)
            stateChanged = stateChanged || view.state != prevState
        }

        if (stateChanged) {
            draw()
        }
    }

}
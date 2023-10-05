package solve.engine.jade

abstract class Component {
    @JvmField
    var gameObject: GameObject? = null
    open fun start() {}
    abstract fun update(dt: Float)
}
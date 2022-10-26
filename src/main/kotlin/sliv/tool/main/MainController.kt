package sliv.tool.main

import sliv.tool.project.model.*
import sliv.tool.scene.SceneFacade
import sliv.tool.scene.controller.SceneController
import tornadofx.*
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId

class MainController : Controller() {
    private val sceneController: SceneController by inject()
    private val sceneFacade = SceneFacade(sceneController)

    //TODO: temporary solution to fill scene with data
    fun importTestData() {
        val layer = ProjectLayer(LayerKind.KEYPOINT, "ORB")
        val path = Paths.get("test/data/real-data-set1.png")
        val timestamp = LocalDateTime.now().atZone(ZoneId.of("UTC+03:00")).toInstant().toEpochMilli()
        val landmarks = listOf(LandmarkFile(layer, Paths.get("test"), emptyList()))
        val frames = listOf(ProjectFrame(timestamp, path, landmarks))
        sceneFacade.visualize(listOf(layer), frames)
    }
}
package sliv.tool.importer.view

import javafx.geometry.Insets
import javafx.stage.DirectoryChooser
import sliv.tool.importer.controller.ImporterController
import tornadofx.*

class ChoosingDirectoryView : Fragment() {
    private val controller: ImporterController by inject()

    private val directoryChooser = DirectoryChooser()
    override val root = hbox(20) {
        padding = Insets(5.0, 0.0, 0.0, 200.0)
        label {
            bind(controller.directoryPath)
        }
        button("Change") {
            action {
                directoryChooser.title = ("Choose working directory")
                val dir = directoryChooser.showDialog(currentStage)
                controller.directoryPath.value = dir.absolutePath
            }
        }
    }
}
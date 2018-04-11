package analysis.ui

import java.awt.{Color, Toolkit}
import java.io.File
import java.util.Scanner

import scala.collection.mutable
import scala.io.Source
import scala.swing._
import scala.swing.event.{EditDone, SelectionChanged}
import scala.util.Random

object ContextViewerApplication extends SimpleSwingApplication {
  private val appDimensions = Toolkit.getDefaultToolkit.getScreenSize
  private val testInstallId = "a590add0-f56f-4053-8b65-b27fe89c239d"
  private val random = new Random(0)
  private val testInstallToColor = mutable.Map[String, Color]()

  def top: MainFrame = new MainFrame {
    title = "Context Viewer"

    object fileList extends ListView[(String, String)] {
      listData = listNonTestContexts()
      renderer = (list: ListView[_ <: (String, String)], isSelected: Boolean, focused: Boolean, a: (String, String), index: Int) => {
        val data = list.listData(index)
        val label = new Label
        label.text = data._1
        label.opaque = true
        if (!testInstallToColor.contains(data._2)) {
          testInstallToColor.put(data._2, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
        }
        label.background = testInstallToColor(data._2)
        label
      }
    }

    val sessionList = new ListView[String]

    object contextNumber extends TextField {
      maximumSize = new Dimension(maximumSize.width, preferredSize.height)
    }

    val installIdLabel = new Label
    installIdLabel.opaque = true
    val sessionIdLabel = new Label
    val positionLabel = new Label
    val textPane = new TextPane
    textPane.editable = true
    textPane.caret.blinkRate = 100
    val fileTextPane = new TextPane
    contents = new BoxPanel(Orientation.Horizontal) {
      preferredSize = appDimensions
      contents += new BoxPanel(Orientation.Vertical) {
        contents += new ScrollPane(fileList)
        preferredSize = new Dimension(50, appDimensions.height)
      }
      contents += new BoxPanel(Orientation.Vertical) {
        contents += contextNumber
        contents += installIdLabel
        contents += sessionIdLabel
        contents += positionLabel
        val scrollPane = new ScrollPane(textPane)
        contents += scrollPane
      }
      contents += new BoxPanel(Orientation.Vertical) {
        preferredSize = new Dimension(400, appDimensions.height)
        maximumSize = preferredSize
        contents += new BorderPanel {
          add(sessionList, BorderPanel.Position.North)
          add(new ScrollPane(fileTextPane), BorderPanel.Position.Center)
        }
      }
    }

    def updateView(): Unit = {
      val (installId, sessionId, position, sourceCode) = readContext(contextNumber.text)
      installIdLabel.text = installId
      installIdLabel.background = testInstallToColor(installId)
      fileTextPane.text = ""
      sessionList.listData = findRelevant(sessionId)
      sessionIdLabel.text = sessionId
      positionLabel.text = position
      textPane.text = sourceCode
      textPane.caret.position = position.toInt
      textPane.requestFocus()
    }

    listenTo(contextNumber, sessionList.selection, fileList.selection)

    reactions += {
      case SelectionChanged(`sessionList`) =>
        if (sessionList.selection.items.nonEmpty) {
          val relevantItemPath = sessionList.selection.items(0)
          fileTextPane.text =
            Source
              .fromFile(new File("/Users/niksaz/Downloads/data/" + relevantItemPath))
              .getLines()
              .mkString("\n")
          textPane.requestFocus()
        }
      case SelectionChanged(`fileList`) =>
        contextNumber.text = fileList.selection.items(0)._1
        updateView()
      case EditDone(`contextNumber`) =>
        updateView()
    }
  }

  private def listNonTestContexts(): Seq[(String, String)] = {
    val contextsPath = new File("/Users/niksaz/Downloads/data/contexts")
    contextsPath.listFiles().map { file =>
      val scanner = new Scanner(file)
      val installId = scanner.nextLine()
      scanner.close()
      (file.getName, installId)
    }.filterNot(_._2 == testInstallId).sortBy(_._1.toInt)
  }

  private def readContext(contextName: String): (String, String, String, String) = {
    val contextsPath = new File("/Users/niksaz/Downloads/data/contexts")
    val contextPath = contextsPath.toPath.resolve(contextName)
    val scanner = new Scanner(contextPath)
    val lines = mutable.ListBuffer[String]()
    while (scanner.hasNext) {
      lines.append(scanner.nextLine())
    }
    scanner.close()
    (lines.head, lines(1), lines(2), lines.drop(3).mkString("\n"))
  }

  private def findRelevant(sessionId: String): Seq[String] = {
    val clicksPath = new File("/Users/niksaz/Downloads/data/clicks")
    val clicks = clicksPath.listFiles().filter { file =>
      val scanner = new Scanner(file)
      val clickSessionId = scanner.nextLine()
      scanner.close()
      clickSessionId.equals(sessionId)
    }.map("clicks/" + _.getName)
    val questionsPath = new File("/Users/niksaz/Downloads/data/questions")
    val questions = questionsPath.listFiles().filter { file =>
      val scanner = new Scanner(file)
      val questionSessionId = scanner.nextLine()
      scanner.close()
      questionSessionId.equals(sessionId)
    }.map("questions/" + _.getName)
    val helpfulsPath = new File("/Users/niksaz/Downloads/data/helpful")
    val helpfuls = helpfulsPath.listFiles().filter { file =>
      val scanner = new Scanner(file)
      val helpfulSessionId = scanner.nextLine()
      scanner.close()
      helpfulSessionId.equals(sessionId)
    }.map("helpful/" + _.getName)
    Seq(clicks, questions, helpfuls).flatten
  }
}

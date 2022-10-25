package example

import com.raquo.laminar.api.L._
import org.scalajs.dom
import zio.*


////////////////////////////////////////////// Main

object MainFrontEnd:
  def appComponent = 
    val myDiv =
      div("look at the console (F12) to see the results.")
    myDiv
    
  def main(args: Array[String]): Unit =
    val _ = documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app")
      appContainer.innerHTML = ""
      val _            = render(appContainer, appComponent)
    }(unsafeWindowOwner)
    example.Test.f

////////////////////////////////////////////// Test


package subscript.example

import scala.swing._
import scala.swing.event._

import cats.~>

import freeacp._
import freeacp.LanguageT._
import scala.concurrent.{Future, Promise}

abstract class SimpleSubscriptApplication extends SimpleSwingApplication
                                              with FutureImpl
                                              with ButtonElem
                                              // with CallElem
                                              with KeyElem
                                              with GuiElem {
  override def startup(args: Array[String]) {
    super.startup(args)
    new Thread{override def run={live;quit}}.start()
  }

  val top: MainFrame

  def liveScript: Language
  def live: Unit = liveScript.runM(compiler[Future](defaultCompiler, promiseCompiler), true)
}

trait ButtonElem extends PromiseElem {
  def button(b: Button) = {
    val p = Promise[Result[LanguageT]]()
    lazy val reaction: PartialFunction[Event, Unit] = { case _: ButtonClicked => p.success(ε); b.unsubscribe(reaction); b.enabled = false }
    b.subscribe(reaction)

    call {
      b.enabled = true
      promise(p)
    }
  }
}

trait KeyElem extends PromiseElem { this: SimpleSwingApplication =>
  def key(keyCode: Key.Value) = {
    val p = Promise[Result[LanguageT]]()
    lazy val reaction: PartialFunction[Event, Unit] = { case k: KeyPressed if k.key == keyCode => p.success(ε); top.reactions -= reaction }
    top.reactions += reaction

    promise(p)
  }
}

trait GuiElem extends PromiseElem {
  def gui(task: => Unit) = {
    val p = Promise[Result[LanguageT]]()
    Swing.onEDTWait { task; p.success(ε) }

    promise(p)
  }
}

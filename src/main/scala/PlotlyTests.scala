import plotly.Bar
import plotly.Plotly.{TraceOps, plot}
import plotly.layout.Layout

object PlotlyTests extends App {

  val (x, y) = Seq(
    "Banana" -> 10,
    "Apple" -> 8,
    "Grapefruit" -> 5
  ).unzip

  val lay = Layout()

  //plot(Bar(x,y), lay)
}

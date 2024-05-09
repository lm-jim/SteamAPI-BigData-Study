import plotly._, element._, layout._, Plotly._

object PlotlyTests extends App {

  val x = (0 to 100).map(_ * 0.1)
  val y1 = x.map(d => 2.0 * d + util.Random.nextGaussian())
  val y2 = x.map(math.exp)

  val plot = Seq(
    Scatter(x, y1).withName("Approx twice"),
    Scatter(x, y2).withName("Exp")
  )

  val lay = Layout().withTitle("Curves")
  plot.plot("plot", lay)  // attaches to div element with id 'plot'
}

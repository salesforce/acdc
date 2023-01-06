package utils

import io.prometheus.client.Gauge

object AcdcMetric {
  val dbCountGauge: Gauge = Gauge
    .build()
    .name("acdc_db_record_count")
    .labelNames("table")
    .help("acdc DB table records count")
    .register

  def countDB(table: String, cnt: Double): Unit = {
    dbCountGauge.labels(table).set(cnt)
  }

}

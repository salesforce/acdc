import javax.inject.Inject

import play.api.http.DefaultHttpFilters

import com.salesforce.mce.kineticpulse.MetricFilter

class Filters @Inject() (
  metricsFilter: MetricFilter
) extends DefaultHttpFilters(metricsFilter)

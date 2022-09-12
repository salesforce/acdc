/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

import com.google.inject.AbstractModule

import services.{Metric, PrometheusMetric}
import tasks.{AuthSettingReloadTask, DataInstExpirationTask}
import utils.{Authorization, AuthorizationSettings}

class Module extends AbstractModule {

  override def configure() = {
    // Pass in custom implementation with configs for Authorization
    bind(classOf[Authorization]).toInstance(new Authorization(AuthorizationSettings()))
    // Activate authorization setting reload task
    bind(classOf[AuthSettingReloadTask]).asEagerSingleton()
    // Activate metrics
    bind(classOf[Metric]).to(classOf[PrometheusMetric])
    // Data forget task
    bind(classOf[DataInstExpirationTask]).asEagerSingleton()
  }

}

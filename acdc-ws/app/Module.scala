/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

import com.google.inject.AbstractModule

import utils.{Authorization, AuthorizationSettings}
import tasks.AuthSetttingReloadTask

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Authorization]).toInstance(new Authorization(AuthorizationSettings()))
    bind(classOf[AuthSetttingReloadTask]).asEagerSingleton()
  }

}

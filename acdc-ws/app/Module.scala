import com.google.inject.AbstractModule

import utils.Authorization
import utils.AuthorizationSettings

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Authorization]).toInstance(new Authorization(AuthorizationSettings()))
  }

}

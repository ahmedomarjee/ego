package bio.overture.ego.repository.queryspecification.builder;

import static bio.overture.ego.model.enums.JavaFields.APPLICATIONS;
import static bio.overture.ego.model.enums.JavaFields.GROUP;
import static bio.overture.ego.model.enums.JavaFields.USERGROUPS;
import static bio.overture.ego.model.enums.JavaFields.USERPERMISSIONS;
import static javax.persistence.criteria.JoinType.LEFT;

import bio.overture.ego.model.entity.User;
import java.util.UUID;
import javax.persistence.criteria.Root;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

@Setter
@Accessors(fluent = true, chain = true)
public class UserSpecificationBuilder extends AbstractSpecificationBuilder<User, UUID> {

  private boolean fetchUserPermissions;
  private boolean fetchUserGroups;
  private boolean fetchApplications;

  @Override
  protected Root<User> setupFetchStrategy(Root<User> root) {
    if (fetchApplications) {
      root.fetch(APPLICATIONS, LEFT);
    }
    if (fetchUserGroups) {
      val fromUserGroup = root.fetch(USERGROUPS, LEFT);
      fromUserGroup.fetch(GROUP, LEFT);
    }
    if (fetchUserPermissions) {
      root.fetch(USERPERMISSIONS, LEFT);
    }
    return root;
  }
}
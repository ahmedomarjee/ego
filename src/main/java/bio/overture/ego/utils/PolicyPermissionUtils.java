package bio.overture.ego.utils;

import bio.overture.ego.model.entity.Permission;

import java.util.List;

import static bio.overture.ego.utils.CollectionUtils.mapToList;

public class PolicyPermissionUtils {
  public static String extractPermissionString(Permission permission) {
    return String.format("%s.%s", permission.getPolicy().getName(), permission.getAccessLevel().toString());
  }

  public static List<String> extractPermissionStrings(List<? extends Permission> permissions) {
    return mapToList(permissions, PolicyPermissionUtils::extractPermissionString);
  }
}
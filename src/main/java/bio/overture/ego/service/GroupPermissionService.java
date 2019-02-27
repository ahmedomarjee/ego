package bio.overture.ego.service;

import bio.overture.ego.model.dto.PermissionRequest;
import bio.overture.ego.model.dto.PolicyResponse;
import bio.overture.ego.model.entity.AbstractPermission;
import bio.overture.ego.model.entity.Group;
import bio.overture.ego.model.entity.GroupPermission;
import bio.overture.ego.model.entity.Policy;
import bio.overture.ego.model.exceptions.NotFoundException;
import bio.overture.ego.repository.BaseRepository;
import bio.overture.ego.repository.GroupPermissionRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static bio.overture.ego.model.exceptions.MalformedRequestException.checkMalformedRequest;
import static bio.overture.ego.model.exceptions.NotFoundException.checkNotFound;
import static bio.overture.ego.model.exceptions.UniqueViolationException.checkUnique;
import static bio.overture.ego.utils.CollectionUtils.difference;
import static bio.overture.ego.utils.CollectionUtils.mapToList;
import static bio.overture.ego.utils.Collectors.toImmutableList;
import static bio.overture.ego.utils.Collectors.toImmutableSet;
import static bio.overture.ego.utils.Joiners.COMMA;
import static java.util.UUID.fromString;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class GroupPermissionService extends AbstractPermissionService<GroupPermission> {

  /** Dependencies */
  private final GroupPermissionRepository repository;
  private final GroupService groupService;
  private final PolicyService policyService;

  @Autowired
  public GroupPermissionService(
      @NonNull BaseRepository<GroupPermission, UUID> repository,
      @NonNull GroupPermissionRepository repository1, GroupService groupService,
      @NonNull PolicyService policyService) {
    super(GroupPermission.class, repository);
    this.repository = repository1;
    this.groupService = groupService;
    this.policyService = policyService;
  }

  public void deleteGroupPermissions(
      @NonNull UUID groupId, @NonNull Collection<UUID> permissionsIds) {
    checkMalformedRequest(!permissionsIds.isEmpty(),
        "Must add at least 1 permission for group '%s'", groupId);
    val group = groupService.getGroupWithRelationships(groupId);

    val filteredPermissionMap = group.getPermissions().stream()
        .filter(x -> permissionsIds.contains(x.getId()))
        .collect(toMap(AbstractPermission::getId, identity()));
    val existingPermissionIds = filteredPermissionMap.keySet();
    val nonExistingPermissionIds = difference(permissionsIds, existingPermissionIds);
    checkNotFound(nonExistingPermissionIds.isEmpty(),
        "The following GroupPermission ids for the group '%s' were not found",
        COMMA.join(nonExistingPermissionIds));
    val permissionsToRemove = filteredPermissionMap.values();

    disassociateGroupPermissions(permissionsToRemove);
    getRepository().deleteAll(permissionsToRemove);
  }

  public Group addGroupPermissions(
      @NonNull UUID groupId, @NonNull List<PermissionRequest> permissions) {
    checkMalformedRequest(!permissions.isEmpty(),
        "Must add at least 1 permission for group '%s'", groupId);
    checkUniquePermissionRequests(permissions);
    val group = groupService.getGroupWithRelationships(groupId);
    val newPermissionRequests = resolveUniqueRequests(group, permissions);
    val redundantRequests = difference(permissions, newPermissionRequests);
    checkUnique(redundantRequests.isEmpty(),
        "The following permissions already exist for group '%s': ",
        groupId, COMMA.join(redundantRequests));
    return createPermissions(group, newPermissionRequests);
  }

  public Page<GroupPermission> getGroupPermissions(
      @NonNull UUID groupId, @NonNull Pageable pageable) {
    val groupPermissions = ImmutableList.copyOf(groupService.getGroupWithRelationships(groupId).getPermissions());
    return new PageImpl<>(groupPermissions, pageable, groupPermissions.size());
  }

  private Group createPermissions(Group group, Collection<PermissionRequest> newPermissionRequests){
    val policyIds = newPermissionRequests.stream()
        .map(PermissionRequest::getPolicyId)
        .collect(toImmutableList());

    val policyMap = policyService.getMany(policyIds)
        .stream()
        .collect(toMap(Policy::getId, identity()));

    newPermissionRequests.forEach(x -> createGroupPermission(policyMap, group, x));
    return group;
  }

  /**
   * Disassociates group permissions from its parents
   * @param groupPermissions assumed to be loaded with parents
   */
  private static void disassociateGroupPermissions(Collection<GroupPermission> groupPermissions){
    groupPermissions.forEach( x-> {
          x.getOwner().getPermissions().remove(x);
          x.getPolicy().getGroupPermissions().remove(x);
          x.setPolicy(null);
          x.setOwner(null);
        }
    );
  }

  private static PermissionRequest convertToPermissionRequest(GroupPermission gp){
    return PermissionRequest.builder()
        .mask(gp.getAccessLevel())
        .policyId(gp.getPolicy().getId())
        .build();
  }

  private Set<PermissionRequest> resolveUniqueRequests(Group group, Collection<PermissionRequest> permissionRequests){
    val existingPermissionRequests = group.getPermissions().stream()
        .map(GroupPermissionService::convertToPermissionRequest)
        .collect(toImmutableSet());
    val permissionsRequestSet = ImmutableSet.copyOf(permissionRequests);
    return Sets.difference(permissionsRequestSet, existingPermissionRequests);
  }

  private void createGroupPermission(Map<UUID, Policy> policyMap, Group group, PermissionRequest request){
    val gp = new GroupPermission();
    val policy = policyMap.get(request.getPolicyId());
    gp.setAccessLevel(request.getMask());
    associateGroupPermission(group, gp);
    associateGroupPermission(policy, gp);
    getRepository().save(gp);
  }

  private static void associateGroupPermission(
      @NonNull Policy policy, @NonNull GroupPermission groupPermission) {
    policy.getGroupPermissions().add(groupPermission);
    groupPermission.setPolicy(policy);
  }

  private static void associateGroupPermission(
      @NonNull Group group, @NonNull GroupPermission groupPermission) {
    group.getPermissions().add(groupPermission);
    groupPermission.setOwner(group);
  }

  private void checkUniquePermissionRequests(Collection<PermissionRequest> requests){
    val permMap = requests.stream().collect(groupingBy(PermissionRequest::getPolicyId));
    policyService.checkExistence(permMap.keySet());
    permMap.forEach((policyId, value) -> {
      val accessLevels = value.stream()
          .map(PermissionRequest::getMask) // validate proper conversion
          .collect(toImmutableSet());
      checkUnique(accessLevels.size() < 2,
          "Found multiple (%s) permission requests for policyId '%s': %s",
          accessLevels.size(), policyId,
          COMMA.join(accessLevels));
    });
  }

  @SneakyThrows
  public GroupPermission findByPolicyAndGroup(@NonNull String policyId, @NonNull String groupId) {
    val opt = repository.findByPolicy_IdAndOwner_id(fromString(policyId), fromString(groupId));

    return opt.orElseThrow(() -> new NotFoundException("Permission cannot be found."));
  }

  public void deleteByPolicyAndGroup(@NonNull String policyId, @NonNull String groupId) {
    val perm = findByPolicyAndGroup(policyId, groupId);
    delete(perm.getId());
  }

  public List<GroupPermission> findAllByPolicy(@NonNull String policyId) {
    return ImmutableList.copyOf(repository.findAllByPolicy_Id(fromString(policyId)));
  }

  public List<PolicyResponse> findByPolicy(@NonNull String policyId) {
    val permissions = findAllByPolicy(policyId);
    return mapToList(permissions, this::getPolicyResponse);
  }

  public PolicyResponse getPolicyResponse(@NonNull GroupPermission p) {
    val name = p.getOwner().getName();
    val id = p.getOwner().getId().toString();
    val mask = p.getAccessLevel();
    return PolicyResponse.builder().name(name).id(id).mask(mask).build();
  }
}

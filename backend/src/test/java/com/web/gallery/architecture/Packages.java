package com.web.gallery.architecture;

/** 新規ArchUnitテストで共通利用するパッケージ名定数 */
final class Packages {

  static final String CONTROLLER = "com.web.gallery.presentation.controller";
  static final String REQUEST = "com.web.gallery.presentation.request";
  static final String RESPONSE = "com.web.gallery.presentation.response";
  static final String CONVERTER = "com.web.gallery.presentation.converter";
  static final String SERVICE = "com.web.gallery.application.service";
  static final String SERVICE_IMPL = "com.web.gallery.application.service.impl";
  // SchedulerLockRepositoryは他から参照されず自己完結しているためinfrastructure.schedulerへ集約されており対象外
  static final String REPOSITORY = "com.web.gallery.application.repository";
  // SchedulerLockRepositoryImplはinfrastructure.schedulerへ集約されており対象外
  static final String REPOSITORY_IMPL = "com.web.gallery.infrastructure.persistence.repository";
  static final String MODEL = "com.web.gallery.application.model";
  static final String APPLICATION_HELPER = "com.web.gallery.application.helper";
  static final String APPLICATION_CONFIG = "com.web.gallery.application.config";
  static final String ENTITY = "com.web.gallery.infrastructure.persistence.entity";
  static final String DTO = "com.web.gallery.infrastructure.persistence.dto";
  static final String DOMAIN = "com.web.gallery.domain.model";
  static final String AGGREGATE = "com.web.gallery.application.aggregate";
  static final String POLICY = "com.web.gallery.domain.service";
  static final String EVENT = "com.web.gallery.domain.event";
  static final String SCHEDULER = "com.web.gallery.infrastructure.scheduler";
  // SchedulerLockMapperはinfrastructure.schedulerへ集約されており対象外
  static final String MAPPER = "com.web.gallery.infrastructure.persistence.mapper";
  // ビジネス区分値Enum。SchedulerLockNameEnumはinfrastructure.schedulerへ集約されており対象外
  static final String DOMAIN_ENUMERATION = "com.web.gallery.domain.enumeration";
  static final String EXCEPTION = "com.web.gallery.domain.exception";
  static final String TYPE_HANDLER = "com.web.gallery.infrastructure.persistence.type_handler";

  // オニオンアーキテクチャの4層ルートパッケージ
  static final String DOMAIN_ROOT = "com.web.gallery.domain";
  static final String APPLICATION_ROOT = "com.web.gallery.application";
  static final String INFRASTRUCTURE_ROOT = "com.web.gallery.infrastructure";
  static final String PRESENTATION_ROOT = "com.web.gallery.presentation";

  private Packages() {}
}

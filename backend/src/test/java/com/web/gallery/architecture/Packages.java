package com.web.gallery.architecture;

/** 新規ArchUnitテストで共通利用するパッケージ名定数 */
final class Packages {

  static final String CONTROLLER = "com.web.gallery.controller";
  static final String CONTROLLER_REQUEST = "com.web.gallery.controller.request";
  static final String CONTROLLER_RESPONSE = "com.web.gallery.controller.response";
  static final String SERVICE = "com.web.gallery.application.service";
  static final String SERVICE_IMPL = "com.web.gallery.application.service.impl";
  // SchedulerLockRepositoryを除く。同Repositoryのみ他から参照されず自己完結しているためPhase3でinfrastructure/schedulerへ集約する
  static final String REPOSITORY = "com.web.gallery.application.repository";
  static final String REPOSITORY_IMPL = "com.web.gallery.repository.impl";
  static final String MODEL = "com.web.gallery.application.model";
  static final String ENTITY = "com.web.gallery.entity";
  static final String DTO = "com.web.gallery.dto";
  static final String DOMAIN = "com.web.gallery.domain.model";
  static final String AGGREGATE = "com.web.gallery.domain.aggregate";
  static final String POLICY = "com.web.gallery.domain.service";
  static final String EVENT = "com.web.gallery.domain.event";
  static final String SCHEDULER = "com.web.gallery.scheduler";
  static final String MAPPER = "com.web.gallery.mapper";
  // ビジネス区分値Enum（domain.enumeration）。SchedulerLockNameEnum等インフラ寄りのEnumはENUMERATIONを参照
  static final String DOMAIN_ENUMERATION = "com.web.gallery.domain.enumeration";
  static final String ENUMERATION = "com.web.gallery.enumeration";
  static final String EXCEPTION = "com.web.gallery.domain.exception";
  static final String TYPE_HANDLER = "com.web.gallery.type_handler";

  private Packages() {}
}

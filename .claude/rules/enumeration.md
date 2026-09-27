---
paths:
  - backend/**/enumeration/**
---

# Enumクラスのアーキテクチャルール

ビジネス区分値のEnumは`domain/enumeration/`に配置する。例外：`SchedulerLockNameEnum`は他から参照されず自己完結しているため`infrastructure/scheduler/lock/`に集約する（詳細は`scheduler.md`）。

## 命名規則

- クラス名サフィックス: `Enum`

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/NamingSuffixArchitectureTest.java`のArchUnitテストで機械的に検証される。

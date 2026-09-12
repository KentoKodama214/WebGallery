---
paths:
  - backend/**/mapper/**
---

# Mapper層のアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Mapper`

## 対応XMLファイルの存在

- Mapperインターフェースに対応するXMLファイルが`backend/src/main/resources/com/web/gallery/mapper/`に存在すること
- XMLファイルが存在しないMapperインターフェースは違反

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/MapperArchitectureTest.java`のArchUnitテストで機械的に検証される。

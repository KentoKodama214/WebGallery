---
paths:
  - backend/**/domain/**
---

# Domainクラスのアーキテクチャルール

- `record Xxx(型 xxx) implements Serializable`として実装する
- コンパクトコンストラクタで値のチェックや初期値の代入を行う

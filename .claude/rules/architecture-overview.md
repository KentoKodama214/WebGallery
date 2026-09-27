---
paths:
  - backend/src/main/java/com/web/gallery/**
---

# オニオンアーキテクチャ全体像

backendは`domain` → `application` → `infrastructure`/`presentation`の4層からなるオニオンアーキテクチャで構成される。パッケージ構成の軸はレイヤー優先（layer-first）とし、`com.web.gallery`直下に4つのルートパッケージを置く。

```
com.web.gallery
├── domain/                          【中心・外部依存なし】
│   ├── model/{account,auth,common,inquiry,photo}/   値オブジェクト（record）
│   ├── service/                                      ドメインサービス（Policyサフィックス）
│   ├── event/                                        ドメインイベント定義+Listener
│   ├── exception/
│   ├── enumeration/                                  ビジネス区分値Enum
│   └── constant/                                     Consts, MessageConst, ApiRoutes
│
├── application/                     【ユースケース】
│   ├── model/{機能}/                                 Modelクラス（レイヤー間転送用）
│   ├── aggregate/                                    集約ルート（書き込みユースケースの整合性管理。機能分割なし）
│   ├── service/{機能}/ + service/impl/{機能}/
│   ├── repository/{機能}/                            Repositoryインターフェース（ポート）
│   ├── helper/                                       技術的な処理のポート（JwtTokenProvider等。機能分割なし）
│   └── config/                                       設定値のポート（AccountConfig等。機能分割なし）
│
├── infrastructure/                  【技術詳細・外側】
│   ├── persistence/{entity,dto,mapper,repository}/{機能}/、type_handler/
│   ├── scheduler/                                    定期実行タスク（lock/にSchedulerLock関連を集約）
│   ├── security/                                     JwtTokenProviderImpl, SessionHelper, aspect/, annotation/
│   ├── web/                                          ClientIpResolver, GeoIpResolverImpl, Security/Cors/RateLimit系Config
│   ├── helper/                                       PhotoDirectionResolver等
│   └── config/                                       Configポートの実装（*ConfigImpl）、DataSource/S3/Scheduling等
│
└── presentation/
    ├── controller/{機能}/                            Controllerクラス
    ├── request/{機能}/
    ├── response/{機能}/
    └── converter/{機能}/                             Request→ModelのConverterクラス
```

## 依存方向の原則

同心円の内側は外側を知らない。

- **domain**: 何にも依存しない（最内層）
- **application**: `domain`のみに依存してよい
- **infrastructure・presentation**: `domain`・`application`に依存してよい
- **infrastructure と presentation は互いに外側同士（アダプタ同士）であり、一方向のみ許可する**: presentationはinfrastructureに依存してよいが、infrastructureからpresentationへの依存は禁止する（例: ControllerがSessionHelper・ClientIpResolver等のinfrastructureクラスを直接使うのは許容されるが、逆方向は不可）

この依存方向は`backend/src/test/java/com/web/gallery/architecture/OnionArchitectureTest.java`のArchUnitテストで機械的に検証される。

## Service→Infrastructureの依存逆転を避けるポート化

`application/service/impl/`のServiceクラスは、技術的な処理や設定値取得のために本来infrastructure層のクラスを必要とする場面がある（JWT生成、GeoIP解決、再認証スロットリング、`application.yml`の設定値取得等）。これらはDIP（依存性逆転の原則）に従い、application層にインターフェース（ポート）を定義し、infrastructure層がそれを実装する形にする。

- `application/helper/`: 技術的な処理のポート（`JwtTokenProvider`、`GeoIpResolver`、`ReauthenticationThrottle`）。実装は`infrastructure/security/`・`infrastructure/web/`の`*Impl`クラス
- `application/config/`: 設定値のポート（`AccountConfig`、`LoginConfig`、`JwtConfig`、`InquiryConfig`、`PhotoConfig`）。実装は`infrastructure/config/`の`*ConfigImpl`（`@ConfigurationProperties`で`application.yml`の値をバインド）

インターフェースはサフィックスなし、実装は`Impl`サフィックスとし、既存のService/Repositoryの命名パターン（IFはinner、implはouter）に倣う。

さらに内側の`domain/service/`のPolicyクラスも、設定値が必要な場合はConfig型を直接受け取らず、呼び出し元のService層がConfig値を読み取って`domain/model/`の値オブジェクト（例: `PhotoUpperLimits`、`MaxFileSizeMb`）にラップし、Policyのメソッド引数として渡す。

## Model⇔Request/Dto/Entityの変換

`application/model/`のModelクラスは、外側の層（presentation層のRequest、infrastructure層のDto/Entity）を直接参照しない。変換ロジックは呼び出し元の層に置く。

- **Request → Model**（presentation → application）: `presentation/converter/`の専用Converterクラスに実装する（詳細は`converter.md`）
- **Dto/Entity → Model**（infrastructure → application）: `infrastructure/persistence/repository/`のRepository実装クラス内のprivateメソッドとして実装する（詳細は`repository.md`）
- **Model → Response**（application → presentation）: presentationがapplicationに依存する方向であり問題ないため、従来通りResponseクラス自身の`static from(Model)`ファクトリメソッドで行う（詳細は`response.md`）

## Aggregate（集約）の配置

書き込みユースケースの整合性・ライフサイクル管理を担う集約ルート（`Photo`、`Account`、`Inquiry`）は、ユースケース固有の関心事であるため`domain/`ではなく`application/aggregate/`に配置する。`domain/`には値オブジェクトとドメインサービス（Policy）のみを置く。

## SchedulerLock関連の例外的な集約

`SchedulerLockRepository`（+実装）・`SchedulerLockMapper`（+XML）・`SchedulerLockNameEnum`・`SchedulerLock`（多重起動防止のヘルパー）は、他のどのapplication/domain層からも参照されない完全に自己完結したクラスタである。通常のレイヤー別配置（`application/repository/`、`infrastructure/persistence/{repository,mapper}/`、`domain/enumeration/`）に分散させず、`infrastructure/scheduler/lock/`サブパッケージに丸ごと集約する（詳細は`scheduler.md`）。

## 既知の設計負債（今回のスコープ外）

- `domain/enumeration/ErrorEnum`と`domain/exception/`の相互依存: `domain/`内部に閉じており層境界は破っていないため許容している（詳細は`exception.md`）
- `infrastructure/web/JwtAuthenticationFilter`が`AccountServiceImpl`（インターフェースでなく実装）に直接依存している: 本来のDIP規約（Serviceは常にインターフェース越し）から外れているが、Spring Securityのフィルタチェーン初期化順序の制約により現状は許容している

## 機能別サブパッケージ分割について

`domain/model`・`application/model`・`application/service`（+`service/impl`）・`application/repository`・`infrastructure/persistence/{entity,dto,mapper,repository}`・`presentation/{controller,request,response,converter}`は、`{account,auth,common,inquiry,photo}`の機能別サブパッケージに分かれている。分類は依存関係（対応するEntityやDomainモデルの機能）から逆引きしており、クラス名だけでは判断しない（例: `FileRepository`は実体としてphoto機能、`LoginHistoryRepository`はaccount機能、`RefreshTokenRepository`はauth機能）。

機能分割していないパッケージは以下の通り。いずれもクラス数が少なく機能単位に分けるメリットが薄いため、意図的にフラット構成のままとしている。

- `application/aggregate`（`Account`/`Inquiry`/`Photo`の3クラスのみ）
- `application/helper`・`application/config`（ポートインターフェースのみ）
- `infrastructure/persistence/type_handler`
- `infrastructure/scheduler`（`lock/`サブパッケージのみ）
- `infrastructure/security`・`infrastructure/web`・`infrastructure/helper`・`infrastructure/config`

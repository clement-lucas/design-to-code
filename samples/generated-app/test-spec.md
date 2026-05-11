# テスト仕様書 — プロジェクト管理システム (proman)

## テスト対象アプリケーション
- **アプリ名**: proman (プロジェクト管理システム)
- **フレームワーク**: Spring Boot 3.2.5 / Java 17
- **DB**: H2 (テスト環境)
- **認証**: Spring Security 6.x (フォームログイン)

---

## 1. 単体テスト仕様 (Unit Test Specification)

### 1.1 Service層テスト

#### 1.1.1 ProjectServiceTest
| No. | テストメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|----------|---------|------|
| S01 | searchProjects | 全パラメータnull、Pageable指定 | 全件がPage形式で返却 | 正常 |
| S02 | searchProjects | projectName指定、他null | 名前一致プロジェクトのみ返却 | 正常 |
| S03 | searchProjects | 空文字パラメータ | blankToNullでnull変換後に全件返却 | 境界値 |
| S04 | findById | 存在するprojectId | Optional.of(Project)が返却 | 正常 |
| S05 | findById | 存在しないprojectId | Optional.empty()が返却 | 異常 |
| S06 | createProject | 正常なProjectオブジェクト | versionNo=1がセットされsaveが呼ばれる | 正常 |
| S07 | updateProject | 正常なProjectオブジェクト | saveが呼ばれProjectが返却 | 正常 |
| S08 | findProjectUsers | 存在するprojectId | ProjectUserリストが返却 | 正常 |
| S09 | findAllOrganizations | 条件なし | 全組織リストが返却 | 正常 |
| S10 | findOrganizationById | 存在するid | Optional.of(Organization)が返却 | 正常 |
| S11 | searchClients | clientNameとindustryCode指定 | 条件一致クライアントリスト返却 | 正常 |
| S12 | searchClients | 空文字パラメータ | blankToNull変換後検索 | 境界値 |
| S13 | findClientById | 存在するid | Optional.of(Client)が返却 | 正常 |
| S14 | findProjectsByUserId | 存在するuserId | プロジェクトリスト返却 | 正常 |
| S15 | findProjectsByPeriod | 日付範囲指定 | 期間内プロジェクトリスト返却 | 正常 |

#### 1.1.2 CodeNameServiceTest
| No. | テストメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|----------|---------|------|
| S16 | getCodeList | PROJECT_TYPEのcodeId | CodeNameリスト返却 | 正常 |
| S17 | getCodeMap | PROJECT_TYPEのcodeId | LinkedHashMap<codeValue, codeName>返却 | 正常 |
| S18 | getCodeName | 存在するcodeId/codeValue | コード名称文字列返却 | 正常 |
| S19 | getCodeName | 存在しないcodeValue | 空文字列が返却 | 異常 |

#### 1.1.3 ProjectDownloadServiceTest
| No. | テストメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|----------|---------|------|
| S20 | findByUserId | 存在するuserId | Optional.of(ProjectsByUser)返却 | 正常 |
| S21 | createRequest | userId指定 | status="01"のリクエスト作成・保存 | 正常 |
| S22 | findRequestById | 存在するrequestId | Optional.of(Request)返却 | 正常 |

### 1.2 Security層テスト

#### 1.2.1 LoginUserDetailsTest
| No. | テスト項目 | テスト条件 | 期待結果 | 区分 |
|-----|----------|----------|---------|------|
| U01 | getAuthorities | pmFlag=true | ROLE_USER + ROLE_PROJECT_MANAGER | 正常 |
| U02 | getAuthorities | pmFlag=false | ROLE_USERのみ | 正常 |
| U03 | isAccountNonLocked | failedCount=4 | true | 境界値 |
| U04 | isAccountNonLocked | failedCount=5 | false | 境界値 |
| U05 | isAccountNonExpired | effectiveDateTo=未来日 | true | 正常 |
| U06 | isCredentialsNonExpired | passwordExpDate=未来日 | true | 正常 |
| U07 | isEnabled | effectiveDateFrom=過去日 | true | 正常 |

#### 1.2.2 LoginUserDetailsServiceTest
| No. | テスト項目 | テスト条件 | 期待結果 | 区分 |
|-----|----------|----------|---------|------|
| U08 | loadUserByUsername | 存在するloginId | LoginUserDetails返却 | 正常 |
| U09 | loadUserByUsername | 存在しないloginId | UsernameNotFoundException | 異常 |

---

## 2. 結合テスト仕様 (Integration Test Specification)

### 2.1 Controller結合テスト

#### 2.1.1 TopControllerTest
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C01 | / | GET | 認証済みPMユーザ | 200, view="top", userName表示 | 正常 |
| C02 | / | GET | 未認証 | 302→/login | 異常 |

#### 2.1.2 ProjectControllerTest — プロジェクト登録
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C03 | /project/create | GET | ROLE_PROJECT_MANAGER | 200, view="project/create" | 正常 |
| C04 | /project/create | GET | ROLE_USER (PM以外) | 403 Forbidden | 異常 |
| C05 | /project/create | GET | 未認証 | 302→/login | 異常 |
| C06 | /project/create/confirm | POST | 有効なフォーム + PM | 200, view="project/confirmCreate" | 正常 |
| C07 | /project/create/confirm | POST | 必須項目未入力 + PM | 200, view="project/create" + errors | 異常 |
| C08 | /project/create/confirm | POST | 終了日<開始日 + PM | バリデーションエラー | 異常 |
| C09 | /project/create/execute | POST | 有効なフォーム + PM | 302→/project/create/complete | 正常 |
| C10 | /project/create/execute | POST | back パラメータあり + PM | 200, view="project/create" | 正常 |
| C11 | /project/create/complete | GET | PM認証済み | 200, view="project/completeCreate" | 正常 |

#### 2.1.3 ProjectControllerTest — プロジェクト検索
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C12 | /project/search | GET | 認証済み | 200, view="project/search" | 正常 |
| C13 | /project/search/search | GET | 検索条件なし | 200, 全件表示 | 正常 |
| C14 | /project/search/search | GET | 存在しない条件 | 200, エラーメッセージ表示 | 異常 |

#### 2.1.4 ProjectControllerTest — プロジェクト詳細
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C15 | /project/detail?projectId=1 | GET | 存在するID + 認証済み | 200, view="project/detail" | 正常 |
| C16 | /project/detail?projectId=999 | GET | 存在しないID | IllegalArgumentException | 異常 |

#### 2.1.5 ProjectControllerTest — プロジェクト更新
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C17 | /project/update?projectId=1 | GET | PM認証済み + 存在するID | 200, view="project/update" | 正常 |
| C18 | /project/update/confirm | POST | 有効なフォーム + PM | 200, view="project/confirmUpdate" | 正常 |
| C19 | /project/update/confirm | POST | バリデーションエラー + PM | 200, view="project/update" | 異常 |
| C20 | /project/update/execute | POST | 有効なフォーム + PM | 302→/project/update/complete | 正常 |
| C21 | /project/update/execute | POST | back パラメータあり + PM | 200, view="project/update" | 正常 |

#### 2.1.6 ClientControllerTest
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |
|-----|-------------|------------|----------|---------|------|
| C22 | /client/search | GET | 認証済み | 200, view="client/search" | 正常 |
| C23 | /client/search/search | GET | clientName指定 | 200, クライアント一覧表示 | 正常 |

### 2.2 認証・認可テスト
| No. | テスト対象URL | ロール | テスト条件 | 期待結果 | 区分 |
|-----|-------------|-------|----------|---------|------|
| A01 | /login | 未認証 | GETアクセス | 200 (ログイン画面) | 正常 |
| A02 | / | 未認証 | GETアクセス | 302→/login | 正常 |
| A03 | /project/create | ROLE_USER | GETアクセス | 403 | 正常 |
| A04 | /project/create | ROLE_PROJECT_MANAGER | GETアクセス | 200 | 正常 |
| A05 | /project/update/** | ROLE_USER | GETアクセス | 403 | 正常 |
| A06 | /project/upload/** | ROLE_USER | GETアクセス | 403 | 正常 |
| A07 | /project/search | ROLE_USER | GETアクセス | 200 | 正常 |
| A08 | /css/** | 未認証 | GETアクセス | 200 (permitAll) | 正常 |

---

## 3. システムテスト仕様 (System Test Specification)

### 3.1 機能テスト
| No. | 機能ID | 機能名 | テストシナリオ | 期待結果 | 区分 |
|-----|--------|-------|-------------|---------|------|
| F01 | WA10201 | プロジェクト登録 | 入力→確認→実行→完了 | プロジェクトがDBに登録される | 正常 |
| F02 | WA10201 | プロジェクト登録(戻る) | 入力→確認→戻る→再入力 | フォーム値が保持される | 正常 |
| F03 | WA10202 | プロジェクト検索 | 条件入力→検索実行 | 条件一致結果が一覧表示 | 正常 |
| F04 | WA10203 | プロジェクト更新 | 詳細→更新→確認→実行 | プロジェクトがDB更新される | 正常 |
| F05 | WA10204 | プロジェクト詳細 | 一覧→詳細表示 | プロジェクト情報が正しく表示 | 正常 |

### 3.2 業務スルーテスト
| No. | シナリオ名 | 操作手順 | 期待結果 | 区分 |
|-----|----------|---------|---------|------|
| B01 | プロジェクト登録～検索～詳細 | PM でログイン→プロジェクト登録→検索→詳細確認 | 登録したプロジェクトが検索・詳細で確認可能 | 正常 |
| B02 | プロジェクト登録～更新 | PM でログイン→プロジェクト登録→更新→完了 | 更新内容がDB反映される | 正常 |

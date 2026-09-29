# JWT Specification

Please modify this specification according to your project requirements.

### Org API plane

| 字段名            | 含义                                                                       | 必填  |
|----------------|--------------------------------------------------------------------------|-----|
| iss            | Token颁发者的URL，系统通过iss来判定所属的API plane                                      | 必填  |
| org_id         | 所属org的ID                                                                 | 必填  |
| member_id      | 仅针对Member，表示Member ID，此时必填                                               | 非必填 |
| principal_type | Principal类型，对于member固定为MEMBER，<br/>对于service client固定为ORG_SERVICE_CLIENT | 必填  |

### Platform API plane

| 字段名            | 含义                                                                                    | 必填  |
|----------------|---------------------------------------------------------------------------------------|-----|
| iss            | Token颁发者的URL，系统通过iss来判定所属的API plane                                                   | 必填  |
| supervisor_id  | 仅针对Supervisor，表示Supervisor ID，此时必填                                                    | 非必填 |
| principal_type | Principal类型，对于supervisor固定为SUPERVISOR，<br/>对于service client固定为PLATFORM_SERVICE_CLIENT | 必填  |
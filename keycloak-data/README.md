# Keycloak data

This `keycloak-data` folder is used for local Keycloak data export/import, exported json files under this folder will be imported when run `start-docker-compose.sh`.

The current folder contains the following settings:

#### platform-realm.json
- A realm named `platform` for hosting platform level users and service clients, it contains:
    - A client named `test-platform-web-portal` for supervisors to login the platform web portal;
    - A user named `test-supervisor`(password:`111111`) for simulating a supervisor user, it has the following claim
      fields  ia `platform_supervisor_context` client scope::
        - `principal_type = SUPERVISOR`;
        - `supervisor_id = SUP1234567890`;
    - A client (Client ID: `test-platform-service-client-001`, Client Secret:`hw0lwxhz5M2cl3ncPE79geNEEIcPhNR2`) for simulating a platform
      level service client, it has the following claim fields:
        - `principal_type = PLATFORM_SERVICE_CLIENT`;

#### org-realm.json
- A realm named `org` for hosting org level users and service clients, it contains:
    - A client named `test-org-web-portal` for org members to login the org web portal;
    - A user named `test-org-admin`(password:`111111`) for simulating an org admin member, it has the following claim
      fields via `org_member_context` client scope:
        - `principal_type = MEMBER`
        - `org_id = ORG123456`
        - `member_id = MBR1234567890`
    - A client (Client ID: `test-org-service-client-org123456`, Client Secret:`kyt2TLc5VbHXeiz5MSpvW36oKIx3yGHc`) for
      simulating an org level service client, it has the following claim fields via
      `test-org-service-client-org123456-dedicated` client scope:
        - `principal_type = ORG_SERVICE_CLIENT`
        - `org_id = ORG123456`

# How to import Keycloak data
 The `docker-compose.yml` file mounts the `keycloak-data` folder into Keycloak Docker's `/opt/keycloak/data/import` folder from where Keycloak will import all json files automatically at startup.
```
    volumes:
      - ./keycloak-data:/opt/keycloak/data/import
```

# How to export Keycloak data

Sometimes you may want to export your local Keycloak data for future import, use the following steps:

1. Start Keycloak docker server using `start-docker-compose.sh` and configure Keycloak as per your need.
2. Stop the Keycloak docker server using `docker compose down`, this command stops the Keycloak server but keeps its
   data volumes. This needs to be done before exporting because Keycloak will not allow you to export data while it is
   running, as Keycloak's local database allows only one connection.
3. Run the following command to export the `platform` realm data:

```bash
docker run --rm \
-v andy-springboot4-template_andy-springboot4-template-keycloak-volume:/opt/keycloak/data \
-v $(pwd)/keycloak-data:/tmp/export \
quay.io/keycloak/keycloak:26.2.5 \
export \
--dir /tmp/export \
--realm platform \
--users realm_file
```

4. Run the following command to export the `org` realm data:

```bash
docker run --rm \
-v andy-springboot4-template_andy-springboot4-template-keycloak-volume:/opt/keycloak/data \
-v $(pwd)/keycloak-data:/tmp/export \
quay.io/keycloak/keycloak:26.2.5 \
export \
--dir /tmp/export \
--realm org \
--users realm_file
```

5. Data will be exported to the `keycloak-data` folder.
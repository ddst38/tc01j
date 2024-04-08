# uc-ctrl-rest-openapi

test
## Swagger :

http://localhost:8080/swagger-ui/index.html

## Actuator :

http://localhost:8081/actuator/health

## Démarrer Keycloak :
``` 
keycloak-realm\run_keycloak.bat
```

ou

```
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./keycloak-realm:/opt/keycloak/data/import quay.io/keycloak/keycloak:22.0.5 start-dev --import-realm
```

Infos Keycloak : 

- Admin : http://localhost:8080 user: `admin`, password: `admin`
- Realm : `test`
  - Endpoints : http://localhost:8080/realms/test/.well-known/openid-configuration
  - Clients de test
    - Client 1 : client-id: `test-client`, client-secret: `zHVe7t0W1RVrU3lmJZvH3uaCrmChpQuX`
    - Client 2 : client-id: `test-server`, client-secret: `SoaXfO8lPLYUcftIjsZcD3hEnBwMTZ4g`
  - Users de test :
    - User 1 : username: `user-demandeur`, password : `user-demandeur`, role `ISI_DEMANDEUR`
    - User 2 : username: `user-admin`, password : `user-admin`, role `ISI_ADMIN`

Au besoin, pour réexporter le realm Keycloak, exécuter dans le container :

```
cd /opt/keycloak/bin/
./kc.sh export --file /tmp/test-realm.json --realm test
```

Puis récupérer le fichier `/tmp/test-realm.json` produit.
##  Obtenir un jeton : 
```
curl --request POST \
  --url http://localhost:8080/realms/test/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data client_id=test-client \
  --data grant_type=password \
  --data client_secret=zHVe7t0W1RVrU3lmJZvH3uaCrmChpQuX \
  --data scope=openid \
  --data username=user-demandeur \
  --data password=user-demandeur
```

## Test : 
```
curl --request GET \
  --url http://localhost:8081/beneficiaires/1 \
  --header 'Authorization: Bearer eyJhbGc...' \
```
(utiliser le jeton obtenu précédemment)


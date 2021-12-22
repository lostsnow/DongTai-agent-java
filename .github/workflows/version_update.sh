OLD_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
NEW_VERSION=$1

echo "curent path: `pwd`, change version $OLD_VERSION to $NEW_VERSION"

sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" dongtai-jakarta-api/src/main/java/cn/huoxian/iast/api/ResponseWrapper.java
sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" dongtai-servlet-api/src/main/java/cn/huoxian/iast/api/ResponseWrapper.java
sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" iast-agent/src/main/java/com/secnium/iast/agent/Constant.java

# versions:set is a feature in JDK 1.7+ and Maven 3.3.1+
mvn -q versions:set -DnewVersion="${NEW_VERSION}"
mvn -q versions:update-child-modules
mvn -q versions:commit

git config --global user.name 'exexute'
git config --global user.email '1528360120@qq.com'
git add .
git commit -m "Update: change version from $OLD_VERSION to $NEW_VERSION"

git push "https://$GITHUB_ACTOR:$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY.git" HEAD:main

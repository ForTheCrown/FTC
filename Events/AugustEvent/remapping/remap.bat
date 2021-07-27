java -jar SpecialSource.jar -i AugustEvent-1.0-SNAPSHOT.jar -o s1.jar --srg-in moj-obf.txt --reverse -l -L
java -jar SpecialSource.jar -i s1.jar -o s2.jar --srg-in obf-spigot.csrg -l -L
java -jar SpecialSource.jar -i s2.jar -o AugustPlugin.jar --srg-in spigot-spigot-fields.csrg --reverse -l -L
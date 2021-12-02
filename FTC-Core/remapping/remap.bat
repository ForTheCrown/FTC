java -jar SpecialSource.jar -i Core.jar -o Core-s1.jar --srg-in moj-obf.txt --reverse -l -L --progress-interval 100
java -jar SpecialSource.jar -i Core-s1.jar -o Core-s2.jar --srg-in obf-spigot.csrg -l -L --progress-interval 100
java -jar SpecialSource.jar -i Core-s2.jar -o ForTheCrown-Plugin.jar --srg-in spigot-spigot-fields.csrg --reverse -l -L --progress-interval 100
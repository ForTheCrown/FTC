import * as Helper from "@ftc.utils.io.SerializationHelper";
import * as CharSets from "@nio.charset.StandardCharsets"
import * as Files from "@nio.file.Files";

function loadJsonFile(filePath) {
  let path = getWorkingDirectory().resolve(filePath);

  if (!Files.exists(path)) {
    throw `JSON file ${path} doesn't exist`;
  }

  let string = Files.readString(path, CharSets.UTF_8);
  return JSON.parse(string);
}

function saveJsonFile(filePath, value) {
  let path = getWorkingDirectory().resolve(filePath);

  Helper.writeFile(path, file => {
    let stringified = JSON.stringify(value);
    Files.writeString(file, stringified, CharSets.UTF_8);
  });
}

function getWorkingDirectory() {
  return _script.getWorkingDirectory();
}
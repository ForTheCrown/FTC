package net.forthecrown.scripts.preprocessor;

import net.forthecrown.scripts.Script;

interface PreProcessorCallback {

  void postProcess(Script script);
}

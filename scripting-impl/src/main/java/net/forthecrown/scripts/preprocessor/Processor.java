package net.forthecrown.scripts.preprocessor;

import java.util.List;

interface Processor {

  void run(StringBuffer buffer, List<PreProcessorCallback> callbacks);
}

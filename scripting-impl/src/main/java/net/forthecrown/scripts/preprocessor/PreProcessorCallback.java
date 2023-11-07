package net.forthecrown.scripts.preprocessor;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.scripts.Script;
import net.forthecrown.utils.Result;

interface PreProcessorCallback {

  Result<Unit> postProcess(Script script);
}

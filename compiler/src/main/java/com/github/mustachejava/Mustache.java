package com.github.mustachejava;

import java.io.Writer;

/**
* Default Mustache
*/
public class Mustache extends Code {
  private Code[] codes;

  public Mustache(TemplateContext tc, MustacheFactory cf, Code[] codes, String name) {
    super(tc, cf.getObjectHandler(), null, name, null);
    this.codes = codes;
  }

  public Code[] getCodes() {
    return codes;
  }

  public void setCodes(Code[] newcodes) {
    codes = newcodes;
  }

  @Override
  public void identity(Writer writer) {
    // No self output at the top level
    runIdentity(writer);
  }
}

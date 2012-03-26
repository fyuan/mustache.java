package com.github.mustachejava.codes;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.TemplateContext;

/**
 * Name a section: {{$name}}...{{/name}}
 */
public class ExtendNameCode extends Code {
  public ExtendNameCode(TemplateContext templateContext, DefaultMustacheFactory cf, Mustache mustache, String variable) {
    super(templateContext, cf.getObjectHandler(), mustache, variable, "$");
  }

  public String getName() {
    return name;
  }

  public void setAppended(String appended) {
    this.appended = appended;
  }

  public String getAppended() {
    return appended;
  }
}

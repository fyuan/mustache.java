package com.github.mustachejava.codes;

import com.github.mustachejava.Code;

import java.io.Writer;

/**
 * Used for compiling down runCodes to a single Code.
 * <p/>
 * User: sam
 * Date: 3/25/12
 * Time: 12:55 PM
 */
public abstract class BaseCode implements Code {

  protected final Code[] codes;

  public BaseCode(Code[] codes) {
    this.codes = codes;
  }

  @Override
  public Writer execute(Writer writer, Object scope) {
    throw new Error();
  }

  public abstract Writer execute(Writer writer, Object[] scopes);

  @Override
  public void identity(Writer writer) {
    throw new Error();
  }

  @Override
  public void append(String text) {
    throw new Error();
  }

  @Override
  public Code[] getCodes() {
    throw new Error();
  }

  @Override
  public void setCodes(Code[] codes) {
    throw new Error();
  }

  @Override
  public void init() {
    throw new Error();
  }
}

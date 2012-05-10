package com.github.mustachejava.scaffold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;

/**
 * Generator scaffolding from a template.
 */
public class Generator {

  @Argument(alias = "l", description = "Specifies the language you want to generate")
  private static String language = "java";

  @Argument(alias = "pacakage", description = "The base namespace of the generated code", required = true)
  private static String p;

  @Argument(alias = "d", description = "The destination directory for the source files", required = true)
  private static String dir;

  @Argument(alias = "t", description = "Top level template from which to start", required = true)
  private static String template;

  @Argument(alias = "s", description = "Template source directories", required = true)
  private static String[] sources;

  public static void main(String[] args) {
    try {
      Args.parse(Generator.class, args);
    } catch (IllegalArgumentException e) {
      Args.usage(Generator.class);
      System.exit(1);
    }

    DefaultMustacheFactory dmf = new DefaultMustacheFactory() {
      String name;
      int depth = 0;

      @Override
      public Reader getReader(final String resourceName) {
        if (name == null) {
          name = resourceName;
        }
        try {
          return find(transform(transform(Arrays.asList(sources), new Function<String, File>() {
            @Override
            public File apply(@Nullable String source) {
              return new File(source, resourceName);
            }
          }), new Function<File, Reader>() {
            @Override
            public Reader apply(@Nullable File file) {
              try {
                return file.exists() && file.isFile() ? new BufferedReader(new FileReader(file)) : null;
              } catch (FileNotFoundException e) {
                return null;
              }
            }
          }), new Predicate<Reader>() {
            @Override
            public boolean apply(@Nullable Reader reader) {
              return reader != null;
            }
          });
        } catch (NoSuchElementException e) {
          return null;
        }
      }

      class Node {
        Node parent;
        String name;
        boolean partial = false;
        boolean value = false;
        boolean hash = false;
        boolean not = false;
        Map<String, Node> children = new HashMap<String, Node>();
      }

      @Override
      public MustacheVisitor createMustacheVisitor() {
        Stack<Node> contexts = new Stack<Node>();
        final MustacheFactory mf = this;
        return new MustacheVisitor() {

          String spaces() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < depth; i++) {
              sb.append("  ");
            }
            return sb.toString();
          }

          @Override
          public Mustache mustache(TemplateContext templateContext) {
            return null;
          }

          @Override
          public void iterable(TemplateContext templateContext, String variable, Mustache mustache) {
            print("iterable: " + variable + ", " + templateContext);
          }

          private void print(String s) {
            System.out.println(spaces() + s);
          }

          @Override
          public void notIterable(TemplateContext templateContext, String variable, Mustache mustache) {
            print("not: " + variable + ", " + templateContext);
          }

          @Override
          public void partial(TemplateContext templateContext, String variable) {
            print("partial: " + variable + ", " + templateContext);
            depth++;
            String file = templateContext.file();
            int index = file.lastIndexOf(".");
            String ext = index == -1 ? "" : file.substring(index);
            mf.compile(variable + ext);
          }

          @Override
          public void value(TemplateContext templateContext, String variable, boolean encoded) {
            print("value: " + variable + ", " + templateContext);
          }

          @Override
          public void write(TemplateContext templateContext, String text) {
          }

          @Override
          public void eof(TemplateContext templateContext) {
            print("mustache: " + templateContext);
          }

          @Override
          public void extend(TemplateContext templateContext, String variable, Mustache mustache) {
            print("extend: " + variable + ", " + templateContext);
          }

          @Override
          public void name(TemplateContext templateContext, String variable, Mustache mustache) {
            print("name: " + variable + ", " + templateContext);
          }

          @Override
          public void startTag(TemplateContext templateContext, String variable) {
            print("start: " + variable);
            depth++;
          }

          @Override
          public void endTag(TemplateContext templateContext, String variable) {
            depth--;
          }
        };
      }
    };

    dmf.compile(template);
  }
}

package de.tudarmstadt.ukp.dkpro.core.io.gigaword.internal;

public class Article {

  private String id;

  private String text;

  public Article(String id, String text) {
    this.id = id;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "Article [id="
        + id
        + ", text="
        + text.substring(0, Math.min(100, text.length() - 1))
        + "...]";
  }

}

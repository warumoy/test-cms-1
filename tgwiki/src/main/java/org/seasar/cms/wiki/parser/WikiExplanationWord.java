/* Generated By:JJTree: Do not edit this line. WikiExplanationWord.java */

package org.seasar.cms.wiki.parser;

public class WikiExplanationWord extends SimpleNode {
  public WikiExplanationWord(int id) {
    super(id);
  }

  public WikiExplanationWord(WikiParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WikiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

  /**
   * token的起始标识
   */
  private final String openToken;
  /**
   * token的结束标识
   */
  private final String closeToken;
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    //    寻找openToken 标识，若没有，则直接返回
    int start = text.indexOf(openToken);
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
//    设置起始位置
    int offset = 0;
//    最终结果
    final StringBuilder builder = new StringBuilder();
//         匹配到 openToken 和 closeToken 之间的表达式
    StringBuilder expression = null;
    while (start > -1) {
//      判断openToken 之前是否是转义字符
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        // 有转义字符，则将转义字符去除
        builder.append(src, offset, start - offset - 1).append(openToken);
//        修改 offset
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
//               没有转义字符，则寻找 closeToken
        if (expression == null) {
          expression = new StringBuilder();
        } else {
//          如果 StringBuilder expression不为空，则重置 expression不为空
          expression.setLength(0);
        }
//        添加 offset 和 openToken 之间的内容，添加到 builder 中
        builder.append(src, offset, start - offset);
        offset = start + openToken.length();
//        寻找结束的 closeToken 的位置
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
//          因为 endToken 前面一个位置是 \ 转义字符，所以忽略 \
//          添加 [offset, end - offset - 1] 的内容 到 builder 中
            expression.append(src, offset, end - offset - 1).append(closeToken);
//          修改 offset ,继续寻找结束的 closeToken 的位置
            offset = end + closeToken.length();
            end = text.indexOf(closeToken, offset);
          } else {
//            如果没有转义字符，则添加 [offset, end - offset] 的内容 到 builder 中，然后设置 offset，跳出本次循环
            expression.append(src, offset, end - offset);
            offset = end + closeToken.length();
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
//          close Token 未找到，则直接拼接。
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
//          如果找到了 closeToken 则将StringBuilder expression交予 tokenHandler 处理并拼接到最后的StringBuilder中
          builder.append(handler.handleToken(expression.toString()));
          offset = end + closeToken.length();
        }
      }
      start = text.indexOf(openToken, offset);
    }
//    拼接剩余部分
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}


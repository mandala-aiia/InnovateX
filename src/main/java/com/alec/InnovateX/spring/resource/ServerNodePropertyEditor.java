package com.alec.InnovateX.spring.resource;

import java.beans.PropertyEditorSupport;

/**
 * 老式属性编辑器（java.beans.PropertyEditor，Spring 前期的转换基石）：
 * 通过 setAsText(字符串) → setValue(对象) 完成 String → ServerNode。
 * 约定格式 "host#port"（故意与 Converter 的 "host:port" 不同，避免两种机制混淆）。
 * <p>
 * 为什么被 ConversionService 取代：
 * <ul>
 *   <li><b>有状态</b>：value 存在编辑器实例里，一个编辑器同一时刻只能服务一次转换；</li>
 *   <li><b>仅适合单线程</b>：并发复用同一个实例会互相覆盖 value；</li>
 *   <li>生命周期古早（源自 Swing/JFC 的 JavaBean 规范），难以表达泛型与复杂类型对。</li>
 * </ul>
 * 但它并未被删除：BeanWrapper / DataBinder 内部仍优先走 PropertyEditor，找不到才退回 ConversionService。
 */
public class ServerNodePropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] parts = text.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("格式应为 host#port，收到: " + text);
        }
        ServerNode node = new ServerNode();
        node.setHost(parts[0].trim());
        node.setPort(Integer.parseInt(parts[1].trim()));
        // 有状态的一步：转换结果暂存在编辑器自身，调用方随后用 getValue() 取走
        setValue(node);
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        if (value instanceof ServerNode node) {
            return node.getHost() + "#" + node.getPort();
        }
        return String.valueOf(value);
    }
}

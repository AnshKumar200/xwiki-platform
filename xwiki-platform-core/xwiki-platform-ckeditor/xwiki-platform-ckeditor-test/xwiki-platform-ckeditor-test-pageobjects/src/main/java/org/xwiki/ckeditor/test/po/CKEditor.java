/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.ckeditor.test.po;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.stability.Unstable;
import org.xwiki.test.ui.XWikiWebDriver;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Models a CKEditor instance.
 *
 * @version $Id$
 * @since 1.13
 */
@Unstable
public class CKEditor extends BaseElement
{
    /**
     * The editor field name.
     */
    private final String name;

    /**
     * Create a new instance that can be used to interact with the specified CKEditor instance.
     * 
     * @param name the editor field name
     */
    public CKEditor(String name)
    {
        this.name = name;
    }

    /**
     * Waits for CKEditor to load.
     *
     * @return this editor instance
     */
    public CKEditor waitToLoad()
    {
        StringBuilder script = new StringBuilder();
        script.append("var name = arguments[0];\n");
        script.append("var callback = arguments[1];\n");
        script.append("require(['deferred!ckeditor'], function(ckeditorPromise) {\n");
        script.append("  ckeditorPromise.done(function(ckeditor) {\n");
        script.append("    // In case the editor instance is not ready yet.\n");
        script.append("    var handler = ckeditor.on('instanceReady', function(event) {\n");
        script.append("      if (name === event.editor.name) {\n");
        script.append("        handler.removeListener();\n");
        script.append("        callback();\n");
        script.append("      }\n");
        script.append("    });\n");
        script.append("    // In case the editor instance is ready.\n");
        script.append("    var instance = ckeditor.instances[name];\n");
        script.append("    instance && instance.status === 'ready' && callback();\n");
        script.append("  });\n");
        script.append("});\n");

        XWikiWebDriver driver = getDriver();
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
        driver.executeAsyncScript(script.toString(), this.name);

        return this;
    }

    /**
     * @return the page object used to interact with the CKEditor tool bar
     * @since 15.5.1
     * @since 15.6RC1
     */
    public CKEditorToolBar getToolBar()
    {
        return new CKEditorToolBar(this);
    }

    /**
     * @return the rich text area
     */
    public RichTextAreaElement getRichTextArea()
    {
        // The in-line frame element is renewed while editing so we can't cache it.
        return new RichTextAreaElement(getIframe());
    }

    /**
     * @return the source text area, but you need to switch to Source first
     */
    public WebElement getSourceTextArea()
    {
        return getDriver().findElementWithoutWaiting(getContainer(), By.className("cke_source"));
    }

    protected WebElement getContainer()
    {
        return (WebElement) getDriver().executeScript("return CKEDITOR.instances[arguments[0]].container.$;",
            this.name);
    }

    protected WebElement getIframe()
    {
        return getDriver().findElementWithoutWaiting(getContainer(), By.className("cke_wysiwyg_frame"));
    }

    /**
     * Execute the runnable on the context of the CKEditor iframe.
     *
     * @param runnable the action to run on the context of the CKEditor iframe
     * @since 14.8RC1
     */
    public void executeOnIframe(Runnable runnable)
    {
        try {
            getDriver().switchTo().frame(getIframe());
            runnable.run();
        } finally {
            getDriver().switchTo().parentFrame();
        }
    }
}

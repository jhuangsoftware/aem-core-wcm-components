/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2017 Adobe Systems Incorporated
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*
                                                  `T",.`-, 
                                                     '8, :. 
                                              `""`oooob."T,. 
                                            ,-`".)O;8:doob.'-. 
                                     ,..`'.'' -dP()d8O8Yo8:,..`, 
                                   -o8b-     ,..)doOO8:':o; `Y8.`, 
                                  ,..bo.,.....)OOO888o' :oO.  ".  `-. 
                                , "`"d....88OOOOO8O88o  :O8o;.    ;;,b 
                               ,dOOOOO""""""""O88888o:  :O88Oo.;:o888d 
                               ""888Ob...,-- :o88O88o:. :o'"""""""Y8OP 
                               d8888.....,.. :o8OO888:: :: 
                              "" .dOO8bo`'',,;O88O:O8o: ::, 
                                 ,dd8".  ,-)do8O8o:"""; ::: 
                                 ,db(.  T)8P:8o:::::    ::: 
                                 -"",`(;O"KdOo::        ::: 
                                   ,K,'".doo:::'        :o: 
                                    .doo:::"""::  :.    'o: 
        ,..            .;ooooooo..o:"""""     ::;. ::;.  'o. 
   ,, "'    ` ..   .d;o:"""'                  ::o:;::o::  :; 
   d,         , ..ooo::;                      ::oo:;::o"'.:o 
  ,d'.       :OOOOO8Oo::" '.. .               ::o8Ooo:;  ;o: 
  'P"   ;  ;.OPd8888O8::;. 'oOoo:.;..         ;:O88Ooo:' O"' 
  ,8:   o::oO` 88888OOo:::  o8O8Oo:::;;     ,;:oO88OOo;  ' 
 ,YP  ,::;:O:  888888o::::  :8888Ooo::::::::::oo888888o;. , 
 ',d: :;;O;:   :888888::o;  :8888888Ooooooooooo88888888Oo; , 
 dPY:  :o8O     YO8888O:O:;  O8888888888OOOO888"" Y8o:O88o; , 
,' O:  'ob`      "8888888Oo;;o8888888888888'"'     `8OO:.`OOb . 
'  Y:  ,:o:       `8O88888OOoo"""""""""""'           `OOob`Y8b` 
   ::  ';o:        `8O88o:oOoP                        `8Oo `YO. 
   `:   Oo:         `888O::oP                          88O  :OY 
    :o; 8oP         :888o::P                           do:  8O: 
   ,ooO:8O'       ,d8888o:O'                          dOo   ;:. 
   ;O8odo'        88888O:o'                          do::  oo.: 
  d"`)8O'         "YO88Oo'                          "8O:   o8b' 
 ''-'`"            d:O8oK  -hrr-                   dOOo'  :o": 
                   O:8o:b.                        :88o:   `8:, 
                   `8O:;7b,.                       `"8'     Y: 
                    `YO;`8b' 
                     `Oo; 8:. 
                      `OP"8.` 
                       :  Y8P 
                       `o  `, 
                        Y8bod. 
                        `""""' 
*/

package com.adobe.cq.wcm.core.components.internal.models.v2;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.internal.servlets.AdaptiveImageServlet;
import com.adobe.cq.wcm.core.components.models.Image;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;

@Model(adaptables = SlingHttpServletRequest.class, adapters = {Image.class, ComponentExporter.class}, resourceType = ImageImpl.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class ImageImpl extends com.adobe.cq.wcm.core.components.internal.models.v1.ImageImpl implements Image {

    public static final String RESOURCE_TYPE = "core/wcm/components/image/v2/image";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageImpl.class);
    private static final String SRC_URI_TEMPLATE_WIDTH_VAR = "{.width}";
    private static final String CONTENT_POLICY_DELEGATE_PATH = "contentPolicyDelegatePath";

    private String srcUriTemplate;

    public ImageImpl() {
        selector = AdaptiveImageServlet.CORE_DEFAULT_SELECTOR;
    }

    @PostConstruct
    protected void initModel() {
        super.initModel();
        boolean altValueFromDAM = properties.get(PN_ALT_VALUE_FROM_DAM, currentStyle.get(PN_ALT_VALUE_FROM_DAM, true));
        boolean titleValueFromDAM = properties.get(PN_TITLE_VALUE_FROM_DAM, currentStyle.get(PN_TITLE_VALUE_FROM_DAM, true));
        displayPopupTitle = properties.get(PN_DISPLAY_POPUP_TITLE, currentStyle.get(PN_DISPLAY_POPUP_TITLE, true));
        if (StringUtils.isNotEmpty(fileReference)) {
            // the image is coming from DAM
            final Resource assetResource = request.getResourceResolver().getResource(fileReference);
            if (assetResource != null) {
                Asset asset = assetResource.adaptTo(Asset.class);
                if (asset != null) {
                    if (!isDecorative && altValueFromDAM) {
                        String damDescription = asset.getMetadataValue(DamConstants.DC_DESCRIPTION);
                        if(StringUtils.isEmpty(damDescription)) {
                            damDescription = asset.getMetadataValue(DamConstants.DC_TITLE);
                        }
                        if (StringUtils.isNotEmpty(damDescription)) {
                            alt = damDescription;
                        }
                    }
                    if (titleValueFromDAM) {
                        String damTitle = asset.getMetadataValue(DamConstants.DC_TITLE);
                        if (StringUtils.isNotEmpty(damTitle)) {
                            title = damTitle;
                        }
                    }
                } else {
                    LOGGER.error("Unable to adapt resource '{}' used by image '{}' to an asset.", fileReference,
                            request.getResource().getPath());
                }
            } else {
                LOGGER.error("Unable to find resource '{}' used by image '{}'.", fileReference, request.getResource().getPath());
            }
        }
        if (hasContent) {
            disableLazyLoading = currentStyle.get(PN_DESIGN_LAZY_LOADING_ENABLED, true);

            srcUriTemplate = baseResourcePath + DOT + selector +
                    SRC_URI_TEMPLATE_WIDTH_VAR + DOT + extension +
                    (inTemplate ? templateRelativePath : "") + (lastModifiedDate > 0 ? "/" + lastModifiedDate + DOT + extension : "");

            // if content policy delegate path is provided pass it to the image Uri
            String policyDelegatePath = request.getParameter(CONTENT_POLICY_DELEGATE_PATH);
            if (StringUtils.isNotBlank(policyDelegatePath)) {
                srcUriTemplate += "?" + CONTENT_POLICY_DELEGATE_PATH + "=" + policyDelegatePath;
                src += "?" + CONTENT_POLICY_DELEGATE_PATH + "=" + policyDelegatePath;
            }

            buildJson();
        }
    }

    @Nonnull
    @Override
    public int[] getWidths() {
        return Arrays.copyOf(smartSizes, smartSizes.length);
    }

    @Override
    public String getSrcUriTemplate() {
        return srcUriTemplate;
    }

    @Override
    public boolean isLazyEnabled() {
        return !disableLazyLoading;
    }

}

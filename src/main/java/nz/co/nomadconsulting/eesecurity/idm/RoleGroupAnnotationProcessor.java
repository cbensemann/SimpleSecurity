/*
 * Copyright 2014 Nomad Consulting Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.nomadconsulting.eesecurity.idm;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;


// TODO not used
@SupportedAnnotationTypes({ "nz.co.nomadconsulting.eesecurity.idm.Roles",
"nz.co.nomadconsulting.eesecurity.idm.RoleGroup" })
public class RoleGroupAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        final Messager messager = processingEnv.getMessager();
        messager.printMessage(Kind.ERROR, "hello");
        for (final TypeElement typeElement : annotations) {
            for (final Element element : roundEnv
                    .getElementsAnnotatedWith(typeElement)) {
                messager.printMessage(Kind.ERROR, "Element is ready");
                if (element.getKind().isField()) {
                    final TypeMirror asType = element.asType();
                    // if (Collections.class.isAssignableFrom(cls))
                }
                else {
                    // is method
                }
            }
        }
        return true;
    }

}

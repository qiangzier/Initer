package com.hzq.initer.compiler;

import com.google.auto.service.AutoService;
import com.hzq.initer.annotation.Inite;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.hzq.initer.compiler.Consts.ANNOTATION_NAME;
import static com.hzq.initer.compiler.Consts.CONTEXT_NAME;
import static com.hzq.initer.compiler.Consts.GENERATE_CLASS_NAME;
import static com.hzq.initer.compiler.Consts.KEY_MODULE_NAME;
import static com.hzq.initer.compiler.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.hzq.initer.compiler.Consts.SEPARATOR;
import static com.hzq.initer.compiler.Consts.TEMPLATE_NAME;
import static com.hzq.initer.compiler.Consts.WARNING_TIPS;

/**
 * 初始化组件代理类生成器
 * Created by hezhiqiang on 2018/11/26.
 */

@AutoService(Processor.class)                       //自动注册注解处理器
@SupportedOptions({KEY_MODULE_NAME})
@SupportedSourceVersion(SourceVersion.RELEASE_7)    //指定使用的Java版本
@SupportedAnnotationTypes(ANNOTATION_NAME)          //指定要处理的注解类型
public class ComponendInitProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Logger logger;
    private Types types;
    private String moduleName = null;   // Module name, maybe its 'app' or others

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        types = processingEnvironment.getTypeUtils();
        logger = new Logger(processingEnvironment.getMessager());
        if(processingEnvironment.getOptions() != null) {
            moduleName = processingEnvironment.getOptions().get(KEY_MODULE_NAME);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(set != null && !set.isEmpty()) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Inite.class);
            try {
                logger.info(">>> Found Processor start ...<<<");
                parseProcessor(elements);
            } catch (IOException e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parseProcessor(Set<? extends Element> elements) throws IOException {
        if(elements != null && !elements.isEmpty()) {
            logger.info(">>> Found processor, size is " + elements.size() + "<<<");

            //template interface
            TypeElement type_componentinit = mElements.getTypeElement(TEMPLATE_NAME);
            TypeElement type_context = mElements.getTypeElement(CONTEXT_NAME);

            /**
             * 生成参数
             * (Context context,boolean isDebug)
             */
            ParameterSpec param_Context = ParameterSpec.builder(ClassName.get(type_context),"context").build();
            ParameterSpec param_Boolean = ParameterSpec.builder(Boolean.class,"isDebug").build();

            /**
             * build method : void init(Context context,boolean isDebug)
             */
            MethodSpec.Builder initMethod = MethodSpec.methodBuilder("init")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(param_Context)
                    .addParameter(param_Boolean);

            for (Element element : elements) {
                logger.info("componendInit#name = " + element.asType().toString());
                TypeMirror tm = element.asType();
                //必须是IComponentInit的子类
                if(types.isSubtype(tm,type_componentinit.asType())) {
                    initMethod.addStatement("new " + tm.toString() + "().init(context,isDebug)");
                } else {
                    throw new RuntimeException("Initer::Compiler >>> Found unsupported class type, type = [" + types.toString() + "] must be implements com.hzq.initer.api.IComponentInit接口");
                }
            }


            /**
             * 生成类文件
             * public class GENERATE_CLASS_NAME implements ILcsComponentInit {
             *      @Override
             *      void init(Context context,boolean isDebug);
             *      @Override
                    void initExp();
             * }
             */

            logger.info(">>> Generated InitImpl start<<< ");
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(GENERATE_CLASS_NAME + SEPARATOR + moduleName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(type_componentinit))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(initMethod.build())

                    .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated InitImpl finish <<< ");
        }
    }
}

package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.util.ResourceTraversal;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.start;
import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;
import static java.util.Optional.*;

/**
 * A fluent utility class to find module versions starting from the module level.
 * It will find {@link ModuleVersion}s based on the given parameters, or based on
 * sensible defaults.
 *
 * @author Carlos Munoz
 */
public class ModuleVersionFinder {

    private final Module module;
    private Optional<Locale> locale = empty();
    private boolean draft = false;
    private Optional<String> variantName = empty();

    private ModuleVersionFinder(Module module) {
        this.module = module;
    }

    /**
     * The starting point for finding a specific module version.
     * @param module The {@link Module} in which to find a specific version
     * @return a finder for the given module.
     */
    static final ModuleVersionFinder forModule(@Nonnull final Module module) {
        if(module == null) {
            throw new RuntimeException("Attempted to build content coordinates without a module");
        }
        return new ModuleVersionFinder(module);
    }

    /**
     * Indicates the finder to find a version in draft state
     * @return
     */
    public ModuleVersionFinder inDraft() {
        draft = true;
        return this;
    }

    /**
     * Indicates the finder to find a released version (this is the default)
     * @return
     */
    public ModuleVersionFinder released() {
        draft = false;
        return this;
    }

    /**
     * Specifies the variant name to find. If a variant name
     * is not specified, the finder will fail.
     * @param variantName
     * @return
     */
    public ModuleVersionFinder withVariant(final String variantName) {
        this.variantName = ofNullable(variantName);
        return this;
    }

    /**
     * Indicates to look in the default variant
     * @return
     */
    public ModuleVersionFinder withDefaultVariant() {
        this.variantName = of(DEFAULT_VARIANT_NAME);
        return this;
    }

    /**
     * Specified the locale in which to find the version. If a locale is not
     * specified, the finder will fail.
     * @param locale
     * @return
     */
    public ModuleVersionFinder withLocale(final Locale locale) {
        this.locale = ofNullable(locale);
        return this;
    }

    public ModuleVersionFinder withDefaultLocale() {
        this.locale = of(DEFAULT_MODULE_LOCALE);
        return this;
    }

    Module getModule() {
        return module;
    }

    Optional<Locale> getLocale() {
        return locale;
    }

    boolean isDraft() {
        return draft;
    }

    Optional<String> getVariantName() {
        return variantName;
    }

    /**
     * Finds the module version based on the parameters. This may fail if not enough parameters have
     * been provided to find a specific module version.
     * @return The {@link ModuleVersion} which matches the given parameters.
     */
    public Optional<ModuleVersion> get() {
        ResourceTraversal<ModuleVersion> traversal = start(module)
                .traverse(m -> m.moduleLocale(locale.get()))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.child(variantName.get(), ModuleVariant.class))
                .traverse(moduleVariant -> draft ? moduleVariant.draft() : moduleVariant.released());

        return traversal.isPresent() ? of(traversal.get()) : empty();
    }
}

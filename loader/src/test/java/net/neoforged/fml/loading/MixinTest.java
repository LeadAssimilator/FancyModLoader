package net.neoforged.fml.loading;

import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinTest extends LauncherTest {
    @Test
    void testMixinApplies() throws Exception {
        installation.setupProductionClient();

        var p = installation.buildModJar("test.jar")
                .withTestmodModsToml(builder -> builder.customize(config -> {
                    var mixinConfig = Config.inMemory();
                    mixinConfig.set("config", "mixins.json");
                    config.set("mixins", List.of(mixinConfig));
                }))
                .addClass("testmod.MixinTargetClass", """
                        import java.util.concurrent.Callable;
                        public class MixinTargetClass implements Callable<String> {
                            public String call() {
                                return "mixin did not apply";
                            }
                        }
                        """)
                .addClass("testmod.mixins.TestMixin", """
                        import org.spongepowered.asm.mixin.Mixin;
                        import org.spongepowered.asm.mixin.injection.At;
                        import org.spongepowered.asm.mixin.injection.Inject;
                        import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
                        @Mixin(testmod.MixinTargetClass.class)
                        class TestMixin {
                            @Inject(method="call", at=@At("HEAD"), cancellable = true)
                            private static void modifyCall(CallbackInfoReturnable<String> cri) {
                                cri.setReturnValue("mixin did apply!");
                            }
                        }
                        """)
                .addTextFile("mixins.json", """
                        {
                          "required": true,
                          "minVersion": "0.8.6",
                          "package": "testmod.mixins",
                          "mixins": [
                            "TestMixin",
                          ]
                        }
                        """)
                .build();

        launchAndLoad("forgeclient");
        var targetClass = Class.forName("testmod.MixinTargetClass", true, gameClassLoader);
        var targetObject = (Callable<?>) targetClass.getConstructor().newInstance();
        assertEquals("mixin did apply!", targetObject.call());
    }
}

package com.david.core.password.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/** 外部化密码编码配置，支持多种算法及参数定制。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.password")
public class PasswordProperties {

	/** 是否启用自动配置。 */
	private boolean enabled = true;

	/** 编码算法，默认为 BCrypt。 */
	private Algorithm algorithm = Algorithm.BCRYPT;

	/** BCrypt 强度，合法范围 4~31。 */
	private int bcryptStrength = 12;

	/** 是否以 Base64 输出 BCrypt 哈希。 */
	private boolean bcryptEncodeHashAsBase64 = false;

	/** PBKDF2 使用的全局密钥（pepper），可为空串。 */
	private String secret = "";

	/** 是否以 Base64 输出 PBKDF2 哈希。 */
	private boolean pbkdf2EncodeHashAsBase64 = false;

	/** PBKDF2 所使用的 SecretKeyFactory 算法。 */
	private String pbkdf2Algorithm =
			Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256.name();

	/** PBKDF2 盐长度（字节）。建议 >= 16。*/
	private int pbkdf2SaltLength = 16;

	/** PBKDF2 派生哈希宽度（位），Spring 默认 256。*/
	private int pbkdf2HashWidth = 256;

	/** PBKDF2 迭代次数，Spring 5.8+/6.x 默认 310000。*/
	private int pbkdf2Iterations = 310_000;

	/** DelegatingPasswordEncoder 默认编码 id。 */
	private String delegatingId = "bcrypt";

	public enum Algorithm {
		/** 使用 BCrypt 算法编码。 */
		BCRYPT,
		/** 使用 PBKDF2 算法编码。 */
		PBKDF2,
		/** 使用 Spring Security 的 DelegatingPasswordEncoder，根据密码前缀动态选择算法。 */
		DELEGATING
	}
}

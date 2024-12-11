package com.pig4cloud.pig.books.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书馆图书信息表
 *
 * @author pumpkin
 * @date 2024-12-11 10:13:51
 */
@Data
@TableName("sys_books")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "图书馆图书信息表")
public class BooksEntity extends Model<BooksEntity> {


	/**
	* 图书ID
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="图书ID")
    private Integer bookId;

	/**
	* 书名
	*/
    @Schema(description="书名")
    private String title;

	/**
	* 作者
	*/
    @Schema(description="作者")
    private String author;

	/**
	* 出版社
	*/
    @Schema(description="出版社")
    private String publisher;

	/**
	* 图书类别
	*/
    @Schema(description="图书类别")
    private String genre;

	/**
	* 出版日期
	*/
    @Schema(description="出版日期")
    private LocalDate publishDate;

	/**
	* 国际标准书号（ISBN）
	*/
    @Schema(description="国际标准书号（ISBN）")
    private String isbn;

	/**
	* 图书价格
	*/
    @Schema(description="图书价格")
    private BigDecimal price;

	/**
	* 库存数量
	*/
    @Schema(description="库存数量")
    private Integer stock;

	/**
	* 可借数量
	*/
    @Schema(description="可借数量")
    private Integer availableStock;

	/**
	* 借阅次数
	*/
    @Schema(description="借阅次数")
    private Integer borrowCount;

	/**
	* 图书简介
	*/
    @Schema(description="图书简介")
    private String description;

	/**
	* 书籍状态
	*/
    @Schema(description="书籍状态")
    private Object status;

	/**
	* 书籍存放位置
	*/
    @Schema(description="书籍存放位置")
    private String location;

	/**
	* 记录创建时间
	*/
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="记录创建时间")
    private LocalDateTime createTime;

	/**
	* 记录更新时间
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description="记录更新时间")
    private LocalDateTime updateTime;
}

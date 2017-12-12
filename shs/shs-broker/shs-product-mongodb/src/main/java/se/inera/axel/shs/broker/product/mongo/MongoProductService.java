/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/**
 * 
 */
package se.inera.axel.shs.broker.product.mongo;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.axel.shs.broker.product.ProductService;
import se.inera.axel.shs.broker.product.mongo.model.MongoShsProduct;
import se.inera.axel.shs.xml.product.ShsProduct;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@Service("productService")
public class MongoProductService implements ProductService {
	@Resource
	protected MongoShsProductRepository mongoShsProductRepository;
	
	@Autowired
    protected ProductAssembler assembler;

	/* (non-Javadoc)
	 * @see se.inera.axel.shs.broker.product.ProductService#getProduct(java.lang.String)
	 */
	@Override
	public ShsProduct getProduct(String productTypeId) {
		ShsProduct product = null;
		MongoShsProduct mongoShsProduct = mongoShsProductRepository.findOne(productTypeId);
		
		if (mongoShsProduct != null) {
			product = assembler.assembleShsProduct(mongoShsProduct); 
		}
		
		return product;
	}

    @Override
   	public List<ShsProduct> findAll() {
   		Iterable<MongoShsProduct> list = mongoShsProductRepository.findAll();
   		return assembler.assembleShsProductList(list);
   	}


}

import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository, mapToProduct, mapToProducts } from '../api';
import { ProductFormData, StockAdjustmentData, productManagementApi } from '@features/productManagement';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProducts: (state) => state.products,
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    getInStockProducts: (state) => 
      state.products.filter(product => product.inStock),
  },
  
  actions: {
    // 실제 API 호출로 상품 목록 가져오기
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.products = await productRepository.getProducts();
      } catch (err: any) {
        this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    // 단일 상품 상세 정보 가져오기
    async fetchProductById(id: number) {
      // 이미 상품 목록에 있는 경우 API 호출 스킵
      const existingProduct = this.getProductById(id);
      if (existingProduct && existingProduct.description) {
        return existingProduct;
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const product = await productRepository.getProduct(id);
        
        // 상품 목록에 이미 존재하는 상품인지 확인하고 업데이트 또는 추가
        const index = this.products.findIndex(p => p.id === id);
        if (index !== -1) {
          this.products[index] = product;
        } else {
          this.products.push(product);
        }
        
        return product;
      } catch (err: any) {
        this.error = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching product details:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    // 새 상품 생성
    async createProduct(productData: ProductFormData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const createdProductId = await productManagementApi.createProduct(productData);
        
        // 성공적으로 생성된 후 상품 목록을 다시 불러옴
        await this.fetchProducts();
        
        return createdProductId;
      } catch (err: any) {
        this.error = err.message || '상품 등록 중 오류가 발생했습니다.';
        console.error('Error creating product:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 상품 정보 수정
    async updateProduct(id: number, productData: ProductFormData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await productManagementApi.updateProduct(id, productData);
        
        // 성공적으로 수정된 후 상품 목록을 다시 불러옴
        await this.fetchProducts();
        
        return id;
      } catch (err: any) {
        this.error = err.message || '상품 수정 중 오류가 발생했습니다.';
        console.error('Error updating product:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 상품 삭제
    async deleteProduct(id: number) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await productManagementApi.deleteProduct(id);
        
        // 성공적으로 삭제된 후 상품 목록에서 제거
        this.products = this.products.filter(product => product.id !== id);
        
        return true;
      } catch (err: any) {
        this.error = err.message || '상품 삭제 중 오류가 발생했습니다.';
        console.error('Error deleting product:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 재고 조정
    async adjustStock(id: number, adjustmentData: StockAdjustmentData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await productManagementApi.adjustStock(id, adjustmentData);
        
        // 성공적으로 재고 조정 후 상품 목록을 다시 불러옴
        await this.fetchProducts();
        
        return id;
      } catch (err: any) {
        this.error = err.message || '재고 조정 중 오류가 발생했습니다.';
        console.error('Error adjusting stock:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    }
  }
});

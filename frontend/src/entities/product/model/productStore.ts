import { defineStore } from 'pinia';
import { Product, ProductCategory, ProductStock, StockAdjustmentData, ProductData } from './types';
import { productApi } from '@shared/api';

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
        const productsData = await productApi.getProducts();
        
        // API 응답을 애플리케이션 모델에 맞게 변환
        this.products = productsData.map(item => ({
          id: item.id,
          name: item.name,
          originalPrice: item.originalPrice,
          price: item.price,
          description: '', // API에서 받아온 설명
          imageUrl: item.thumbnailImageUrl, // 백엔드의 필드명을 프론트엔드 모델에 맞게 매핑
          thumbnailImageUrl: item.thumbnailImageUrl, // 추가
          category: item.category,
          inStock: item.status !== 'SOLD_OUT', // 품절 상태가 아니면 재고 있음
          status: item.status,
          stock: item.stock ? {
            quantity: item.stock
          } : { quantity: 0 } // 재고 정보 추가
        }));
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
        const productData = await productApi.getProduct(id);
        
        // 상품 상세 정보를 애플리케이션 모델에 맞게 변환
        const product: Product = {
          id: productData.id,
          name: productData.name,
          originalPrice: productData.originalPrice,
          price: productData.price,
          description: productData.description || '',
          imageUrl: productData.thumbnailImageUrl, // 백엔드의 필드명을 프론트엔드 모델에 맞게 매핑
          thumbnailImageUrl: productData.thumbnailImageUrl, // 추가
          category: productData.category,
          inStock: productData.stock > 0 && productData.status !== 'SOLD_OUT',
          status: productData.status,
          stock: {
            quantity: productData.stock
          }
        };
        
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
    async createProduct(productData: ProductData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const createdProductId = await productApi.createProduct(productData);
        
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
    async updateProduct(id: number, productData: ProductData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await productApi.updateProduct(id, productData);
        
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
        await productApi.deleteProduct(id);
        
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
        await productApi.adjustStock(id, adjustmentData);
        
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

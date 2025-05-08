<template>
  <div class="min-h-screen bg-gray-50">
    <div class="bg-white shadow">
      <div class="container mx-auto px-4 py-6">
        <h1 class="text-3xl font-bold text-gray-800">상품 관리</h1>
        <p class="text-gray-600 mt-2">상품 정보를 관리하고 재고를 조정할 수 있습니다</p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-6">
      <div class="flex flex-col gap-6">
        <!-- 관리자 전용 UI -->
        <div v-if="isAdmin" class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold text-gray-800">상품 목록</h2>
            <button 
              @click="openProductForm(null)" 
              class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              새 상품 등록
            </button>
          </div>

          <!-- 상품 목록 테이블 -->
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상품명</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">판매가</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">재고</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                  <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-if="isLoading" class="text-center">
                  <td colspan="6" class="px-6 py-4">
                    <div class="flex justify-center">
                      <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-900"></div>
                    </div>
                  </td>
                </tr>
                <tr v-else-if="products.length === 0" class="text-center">
                  <td colspan="6" class="px-6 py-4 text-gray-500">등록된 상품이 없습니다</td>
                </tr>
                <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <div class="h-10 w-10 flex-shrink-0 mr-4">
                        <img class="h-10 w-10 rounded-full object-cover" :src="product.thumbnailImageUrl" alt="" />
                      </div>
                      <div>
                        <div class="text-sm font-medium text-gray-900">{{ product.name }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ getCategoryDescription(product.category) }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ formatPrice(product.price) }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ product.stock?.quantity || 0 }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span 
                      :class="[
                        'px-2 inline-flex text-xs leading-5 font-semibold rounded-full', 
                        getStatusClass(product.status || '')
                      ]"
                    >
                      {{ getStatusText(product.status || '') }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button 
                      @click="openProductForm(product)" 
                      class="text-indigo-600 hover:text-indigo-900 mr-3"
                    >
                      수정
                    </button>
                    <button 
                      @click="openStockAdjustmentModal(product)" 
                      class="text-green-600 hover:text-green-900 mr-3"
                    >
                      재고 조정
                    </button>
                    <button 
                      @click="deleteProduct(product.id)" 
                      class="text-red-600 hover:text-red-900"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 일반 사용자 UI -->
        <div v-else class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-semibold text-gray-800 mb-4">상품 재고 관리</h2>

          <!-- 상품 목록 테이블 -->
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상품명</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">판매가</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">재고</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                  <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-if="isLoading" class="text-center">
                  <td colspan="6" class="px-6 py-4">
                    <div class="flex justify-center">
                      <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-900"></div>
                    </div>
                  </td>
                </tr>
                <tr v-else-if="products.length === 0" class="text-center">
                  <td colspan="6" class="px-6 py-4 text-gray-500">등록된 상품이 없습니다</td>
                </tr>
                <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <div class="h-10 w-10 flex-shrink-0 mr-4">
                        <img class="h-10 w-10 rounded-full object-cover" :src="product.thumbnailImageUrl" alt="" />
                      </div>
                      <div>
                        <div class="text-sm font-medium text-gray-900">{{ product.name }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ getCategoryDescription(product.category) }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ formatPrice(product.price) }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ product.stock?.quantity || 0 }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span 
                      :class="[
                        'px-2 inline-flex text-xs leading-5 font-semibold rounded-full', 
                        getStatusClass(product.status || '')
                      ]"
                    >
                      {{ getStatusText(product.status || '') }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button 
                      @click="openStockAdjustmentModal(product)" 
                      class="text-green-600 hover:text-green-900"
                    >
                      재고 조정
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 상품 등록/수정 폼 모달 -->
    <ProductFormModal 
      v-if="showProductForm" 
      :productId="selectedProductId" 
      @close="closeProductForm" 
      @submit="handleProductFormSubmit" 
    />

    <!-- 재고 조정 모달 -->
    <StockAdjustmentModal 
      v-if="showStockAdjustmentModal && selectedProduct" 
      :product="selectedProduct" 
      @close="closeStockAdjustmentModal" 
      @submit="handleStockAdjustment" 
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, computed, watch } from 'vue';
import { useProductStore, Product, ProductCategory, ProductStatus } from '@entities/product';
import { useUserStore } from '@entities/user';
import { ProductFormData, StockAdjustmentData, ProductFormModal, StockAdjustmentModal } from '@features/productManagement';
import { UserRole } from "@entities/user";
import {formatPrice} from "@shared/lib";

export default defineComponent({
  name: 'ProductManagementPage',
  components: {
    ProductFormModal,
    StockAdjustmentModal
  },
  setup() {
    const productStore = useProductStore();
    const userStore = useUserStore();

    // 관리자 권한 확인
    const isAdmin = computed(() => {
      return userStore.user?.role === UserRole.ADMIN || false;
    });

    // 상품 데이터
    const products = computed(() => productStore.products);
    const isLoading = computed(() => productStore.isLoading);

    // 상품 폼 모달 상태
    const showProductForm = ref(false);
    const selectedProduct = ref<Product | null>(null);
    const selectedProductId = ref<number | null>(null);

    // 재고 조정 모달 상태
    const showStockAdjustmentModal = ref(false);

    // 데이터 로드
    onMounted(async () => {
      try {
        await productStore.fetchProducts();
      } catch (err) {
        console.error('상품 로드 실패:', err);
      }
    });

    // 상품 폼 열기
    const openProductForm = (product: Product | null) => {
      selectedProduct.value = product;
      selectedProductId.value = product?.id || null;
      showProductForm.value = true;
    };

    // 상품 폼 닫기
    const closeProductForm = () => {
      showProductForm.value = false;
      selectedProduct.value = null;
      selectedProductId.value = null;
    };

    // 상품 폼 제출 처리
    const handleProductFormSubmit = async (productData: ProductFormData) => {
      try {
        if (selectedProduct.value) {
          // 상품 수정
          await productStore.updateProduct(selectedProduct.value.id, productData);
        } else {
          // 새 상품 등록
          await productStore.createProduct(productData);
        }
        closeProductForm();
      } catch (err) {
        console.error('상품 저장 실패:', err);
      }
    };

    // 상품 삭제
    const deleteProduct = async (productId: number) => {
      if (!confirm('정말 이 상품을 삭제하시겠습니까?')) {
        return;
      }

      try {
        await productStore.deleteProduct(productId);
      } catch (err) {
        console.error('상품 삭제 실패:', err);
      }
    };

    // 재고 조정 모달 열기
    const openStockAdjustmentModal = (product: Product) => {
      selectedProduct.value = product;
      showStockAdjustmentModal.value = true;
    };

    // 재고 조정 모달 닫기
    const closeStockAdjustmentModal = () => {
      showStockAdjustmentModal.value = false;
      selectedProduct.value = null;
    };

    // 재고 조정 처리
    const handleStockAdjustment = async (adjustmentData: StockAdjustmentData) => {
      if (!selectedProduct.value) return;

      try {
        await productStore.adjustStock(selectedProduct.value.id, adjustmentData);
        closeStockAdjustmentModal();
      } catch (err) {
        console.error('재고 조정 실패:', err);
      }
    };

    // 상품 상태 표시 클래스
    const getStatusClass = (status: string) => {
      switch (status) {
        case 'ACTIVE':
          return 'bg-green-100 text-green-800';
        case 'SOLD_OUT':
          return 'bg-red-100 text-red-800';
        case 'DISCONTINUED':
          return 'bg-gray-100 text-gray-800';
        default:
          return 'bg-gray-100 text-gray-800';
      }
    };

    // 상품 상태 텍스트
    const getStatusText = (status: string) => {
      switch (status) {
        case 'ACTIVE':
          return '판매중';
        case 'SOLD_OUT':
          return '품절';
        case 'DISCONTINUED':
          return '판매중단';
        case 'DELETED':
          return '삭제됨';
        default:
          return status;
      }
    };

    // 상품 카테고리 설명
    const getCategoryDescription = (category: ProductCategory) => {
      return ProductCategory.getDescription(category);
    };

    return {
      products,
      isLoading,
      isAdmin,
      showProductForm,
      selectedProduct,
      selectedProductId,
      showStockAdjustmentModal,
      openProductForm,
      closeProductForm,
      handleProductFormSubmit,
      deleteProduct,
      openStockAdjustmentModal,
      closeStockAdjustmentModal,
      handleStockAdjustment,
      getStatusClass,
      getStatusText,
      getCategoryDescription,
      formatPrice
    };
  }
});
</script>
